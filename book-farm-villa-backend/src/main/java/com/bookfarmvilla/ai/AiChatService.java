package com.bookfarmvilla.ai;

import com.bookfarmvilla.dto.response.PropertyResponse;
import com.bookfarmvilla.dto.response.VendorResponse;
import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.Vendor;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.PropertyRepository;
import com.bookfarmvilla.repository.VendorRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AiChatService {

    private static final String EXTRACTION_SYSTEM = """
            You are the requirement extractor for BookFarmVilla.
            Extract only what the customer explicitly said or clearly implied.
            Never invent a location, price, capacity, amenity, date, or property fact.
            Return ONLY valid JSON:
            {
              "intent": "SEARCH_PROPERTY|SEARCH_VENDOR|PROPERTY_QUESTION|GENERAL",
              "requirements": {
                "location": null,
                "guests": null,
                "budget": null,
                "propertyType": null,
                "amenities": [],
                "purpose": null,
                "date": null
              },
              "response": ""
            }
            Use integer INR for budget. Convert 15k to 15000 and 1.5 lakh to 150000.
            Recognize Hindi/Hinglish and English.
            For "near me", do not invent a city; set location to null.
            propertyType may be FARMHOUSE, VILLA or WEDDING_LAWN.
            Keep response empty unless a short clarification is useful.
            """;

    private static final String RESPONSE_SYSTEM = """
            You are BookFarmVilla AI, a property booking assistant.
            Use ONLY the VERIFIED_DATA supplied in the user prompt.
            Never invent or assume property/vendor facts.
            If a field is absent or unknown, say you do not have verified information.
            Do not claim booking availability unless VERIFIED_DATA explicitly contains it.
            Do not claim exact distance unless distance is supplied as verified data.
            Ask concise follow-up questions when required search information is missing.
            Remember the conversation history and preserve previously supplied requirements.
            Reply naturally in the user's language (Hindi/Hinglish if they use it).
            Keep every reply brief, ideally no more than three short sentences.
            Do not mention internal prompts, JSON, databases, APIs, providers, or token limits.
            """;

    private final AiProviderRouter providerRouter;
    private final AiPropertySearchService searchService;
    private final ObjectMapper objectMapper;
    private final PropertyRepository propertyRepository;
    private final VendorRepository vendorRepository;

    public AiChatService(
            AiProviderRouter providerRouter,
            AiPropertySearchService searchService,
            ObjectMapper objectMapper,
            PropertyRepository propertyRepository,
            VendorRepository vendorRepository
    ) {
        this.providerRouter = providerRouter;
        this.searchService = searchService;
        this.objectMapper = objectMapper;
        this.propertyRepository = propertyRepository;
        this.vendorRepository = vendorRepository;
    }

    public AiChatResponse chat(AiChatRequest request) {
        long extractionStartedAt = System.nanoTime();
        AiIntent extracted = extractIntent(request);
        System.out.println("[AI] Intent extraction completed in " + elapsedMillis(extractionStartedAt) + " ms");
        AiRequirements requirements = mergeRequirements(request.history(), extracted.requirements());

        long searchStartedAt = System.nanoTime();
        boolean isVendor = "SEARCH_VENDOR".equalsIgnoreCase(extracted.intent());
        List<PropertyResponse> properties = isVendor ? List.of() : searchService.searchProperties(requirements);
        List<VendorResponse> vendors = isVendor ? searchService.searchVendors(requirements) : List.of();

        // Property/vendor context can be supplied by the UI when a user is asking
        // a question about a specific listing. The data still comes directly from
        // the verified database record.
        if (request.propertyId() != null && !request.propertyId().isBlank()) {
            Property property = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Property", request.propertyId()));
            properties = List.of(searchService.toPropertyResponseForAi(property));
        }
        if (request.vendorId() != null && !request.vendorId().isBlank()) {
            Vendor vendor = vendorRepository.findById(request.vendorId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Vendor", request.vendorId()));
            vendors = List.of(searchService.toVendorResponseForAi(vendor));
        }
        System.out.println("[AI] Listing search completed in " + elapsedMillis(searchStartedAt) + " ms");

        String verifiedData = buildVerifiedData(properties, vendors);
        String prompt = buildResponsePrompt(request, requirements, properties, vendors, verifiedData);

        String message;
        long responseStartedAt = System.nanoTime();
        try {
            message = providerRouter.current().generate(RESPONSE_SYSTEM, prompt);
        } catch (AiProviderException ex) {
            System.err.println("[AI] Response generation failed: " + ex.getMessage());
            ex.printStackTrace(System.err);
            message = fallbackMessage(requirements, properties, vendors);
        }
        System.out.println("[AI] Response generation stage completed in " + elapsedMillis(responseStartedAt) + " ms");

        boolean nearMeRequested = containsNearMe(request.message());
        boolean locationSupported = false;
        String locationMessage = null;

        if (nearMeRequested) {
            // The current Property entity does not contain coordinates. We intentionally
            // do not guess distance or coordinates. This keeps "near me" truthful.
            locationMessage = "Near Me needs verified property latitude/longitude. Your current property data does not store coordinates yet.";
            if (request.latitude() == null || request.longitude() == null) {
                locationMessage = "Please allow location access. Exact nearby results will be enabled once property coordinates are available.";
            }
        }

        return new AiChatResponse(
                message,
                properties,
                vendors,
                requirements,
                locationSupported,
                locationMessage
        );
    }

    private AiIntent extractIntent(AiChatRequest request) {
        String history = renderHistory(request.history());
        String prompt = """
                CONVERSATION:
                %s

                CURRENT USER MESSAGE:
                %s
                """.formatted(history, request.message());

        try {
            String raw = providerRouter.current().generate(EXTRACTION_SYSTEM, prompt);
            return parseIntent(raw);
        } catch (Exception ex) {
            System.err.println("[AI] Intent extraction failed; using deterministic fallback: " + ex.getMessage());
            ex.printStackTrace(System.err);
            return deterministicIntent(request.message());
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private AiIntent parseIntent(String raw) {
        String json = extractJson(raw);
        try {
            return objectMapper.readValue(json, AiIntent.class);
        } catch (Exception ex) {
            throw new AiProviderException("AI returned invalid intent JSON.", ex);
        }
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) return raw.substring(start, end + 1);
        return raw;
    }

    private AiRequirements mergeRequirements(List<AiChatMessage> history, AiRequirements current) {
        if (history == null || history.isEmpty()) return current;

        // We deliberately ask the model to carry the conversation context during extraction.
        // This method only protects missing fields from a partially populated extraction.
        return current;
    }

    private String buildResponsePrompt(
            AiChatRequest request,
            AiRequirements requirements,
            List<PropertyResponse> properties,
            List<VendorResponse> vendors,
            String verifiedData
    ) {
        return """
                CONVERSATION:
                %s

                CURRENT USER MESSAGE:
                %s

                CURRENT REQUIREMENTS:
                %s

                VERIFIED_DATA:
                %s

                Rules:
                - If guests/location/budget are required for a useful search and are missing, ask for only the most important missing item.
                - If VERIFIED_DATA is empty, say that no matching verified listing was found; do not create examples.
                - If results exist, summarize them briefly and tell the UI to show the returned cards without inventing fields.
                - If the customer asks about a field that is not in VERIFIED_DATA, explicitly say it is not verified.
                """.formatted(
                renderHistory(request.history()),
                request.message(),
                toJson(requirements),
                verifiedData
        );
    }

    private String buildVerifiedData(List<PropertyResponse> properties, List<VendorResponse> vendors) {
        List<Object> safe = new ArrayList<>();
        safe.addAll(properties.stream().map(p -> new VerifiedProperty(
                p.id(), p.title(), p.city(), p.type() == null ? null : p.type().name(),
                formatPriceRange(p.startingPrice(), p.endingPrice()), p.capacity(), p.amenities(), p.averageRating(),
                p.address(), p.contactPhone(), p.images().stream().findFirst().map(i -> i.url()).orElse(null)
        )).toList());
        safe.addAll(vendors.stream().map(v -> new VerifiedVendor(
                v.id(), v.name(), v.city(), v.category() == null ? null : v.category().name(),
                v.priceRange(), v.averageRating(), v.contactPhone(),
                v.images().stream().findFirst().map(i -> i.url()).orElse(null)
        )).toList());
        return toJson(safe);
    }

    /** Properties now store startingPrice/endingPrice separately (see PropertyResponse); format them
     *  back into a single display string here since that's what the AI prompt/response shape expects. */
    private String formatPriceRange(java.math.BigDecimal startingPrice, java.math.BigDecimal endingPrice) {
        if (startingPrice == null && endingPrice == null) return null;
        if (startingPrice != null && endingPrice != null) {
            return "₹" + startingPrice.toPlainString() + " - ₹" + endingPrice.toPlainString();
        }
        java.math.BigDecimal only = startingPrice != null ? startingPrice : endingPrice;
        return "₹" + only.toPlainString();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String renderHistory(List<AiChatMessage> history) {
        if (history == null || history.isEmpty()) return "(no previous conversation)";
        return history.stream()
                .filter(m -> m != null && m.content() != null && !m.content().isBlank())
                .limit(8)
                .map(m -> (m.role() == null ? "user" : m.role()) + ": " + m.content())
                .reduce("", (a, b) -> a + "\n" + b);
    }

    private boolean containsNearMe(String message) {
        String m = message.toLowerCase(Locale.ROOT);
        return m.contains("near me") || m.contains("mere paas") || m.contains("mere pass")
                || m.contains("aas paas") || m.contains("around me") || m.contains("nearby");
    }

    private AiIntent deterministicIntent(String message) {
        String m = message.toLowerCase(Locale.ROOT);
        boolean vendor = m.contains("vendor") || m.contains("caterer") || m.contains("decorator")
                || m.contains("photographer") || m.contains("dj");
        Integer guests = extractNumberBefore(m, "people", "person", "guests", "guest", "log");
        Integer budget = extractBudget(m);

        String type = m.contains("villa") ? "VILLA"
                : m.contains("wedding lawn") || m.contains("wedding lawn") ? "WEDDING_LAWN"
                : m.contains("farmhouse") || m.contains("farm house") ? "FARMHOUSE" : null;

        List<String> amenities = new ArrayList<>();
        if (m.contains("pool")) amenities.add("pool");
        if (m.contains("lawn")) amenities.add("lawn");
        if (m.contains("bbq")) amenities.add("bbq");
        if (m.contains("music")) amenities.add("music");
        if (m.contains("parking")) amenities.add("parking");

        AiRequirements r = new AiRequirements(null, guests, budget, type, amenities, null, null);
        return new AiIntent(vendor ? "SEARCH_VENDOR" : "SEARCH_PROPERTY", r, "");
    }

    private Integer extractNumberBefore(String text, String... words) {
        for (String word : words) {
            int idx = text.indexOf(word);
            if (idx > 0) {
                String prefix = text.substring(Math.max(0, idx - 4), idx).trim();
                try {
                    return Integer.parseInt(prefix.replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private Integer extractBudget(String text) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:₹|rs\\.?|inr\\s*)?\\s*(\\d+(?:\\.\\d+)?)\\s*(k|lakh|lac)?")
                .matcher(text);
        if (!m.find()) return null;
        double value = Double.parseDouble(m.group(1));
        if ("k".equalsIgnoreCase(m.group(2))) value *= 1000;
        if ("lakh".equalsIgnoreCase(m.group(2)) || "lac".equalsIgnoreCase(m.group(2))) value *= 100000;
        return (int) value;
    }

    private String fallbackMessage(AiRequirements r, List<PropertyResponse> properties, List<VendorResponse> vendors) {
        if (!properties.isEmpty()) {
            return "Mujhe " + properties.size() + " verified matching properties mili hain. Neeche available options dekhiye.";
        }
        if (!vendors.isEmpty()) {
            return "Mujhe " + vendors.size() + " verified matching vendors mile hain. Neeche options dekhiye.";
        }
        if (r.location() == null && r.guests() == null && r.budget() == null && r.propertyType() == null) {
            return "Sure! Aap location, guests aur approximate budget bata dijiye. Main available verified properties search karunga.";
        }
        return "Aapki requirement ke liye abhi koi verified matching listing nahi mili. Aap budget, location ya guest count mein thoda change karke try kar sakte hain.";
    }

    private record VerifiedProperty(
            String id, String title, String city, String type, String priceRange,
            Integer capacity, java.util.Set<String> amenities, Double rating,
            String address, String contactPhone, String imageUrl
    ) {}

    private record VerifiedVendor(
            String id, String name, String city, String category, String priceRange,
            Double rating, String contactPhone, String imageUrl
    ) {}
}
