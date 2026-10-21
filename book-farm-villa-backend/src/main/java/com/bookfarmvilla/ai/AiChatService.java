package com.bookfarmvilla.ai;

import com.bookfarmvilla.dto.response.PropertyResponse;
import com.bookfarmvilla.dto.response.VendorResponse;
import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.Vendor;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.PropertyRepository;
import com.bookfarmvilla.repository.VendorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AiChatService {
    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private static final String EXTRACTION_SYSTEM = """
            You are the requirement extractor for BookFarmVilla.
            Extract only what the customer explicitly said or clearly implied.
            Never invent a location, price, capacity, amenity, date, or property fact.
            Return ONLY valid JSON:
            {
              "intent": "SEARCH_PROPERTY|SEARCH_VENDOR|PROPERTY_QUESTION|COMPARE|BOOK_ENQUIRY|GENERAL|OFF_TOPIC",
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
            propertyType may be FARMHOUSE, VILLA, WEDDING_LAWN, or a vendor category.
            For an Airbnb-style stay or other venue that does not map to those types, leave
            propertyType null and search verified listings without inventing a new type.
            Classify the user's actual intent, including short searches, refinements,
            questions about one verified listing, comparisons, enquiry requests, small talk,
            vendor searches, and off-topic questions.
            Carry forward requirements already provided in conversation history unless corrected.
            Keep response empty unless a short clarification is useful.
            """;

    private static final String RESPONSE_SYSTEM = """
            You are BookFarmVilla's concise, friendly venue and vendor enquiry assistant.
            Reply in simple English unless the latest user message is Hinglish in Roman script;
            in that case, reply in simple Hinglish. Keep the same language on every turn.
            Use ONLY VERIFIED_DATA for every property/vendor fact. Never invent listing names,
            prices, amenities, availability, capacity, distances, or other details. Listing data
            must be displayed as cards supplied separately by the application.
            When no exact match exists, never stop at "we don't have properties": show the real
            closest alternatives supplied in VERIFIED_DATA and plainly say which filter was relaxed.
            Relax budget first, then location; consider another venue type only after those steps.
            Ask at most one short follow-up question, only for an important missing requirement.
            Bold key requirement words with **markdown**. Do not repeat information the user gave.
            A booking is only an enquiry: a booking expert calls the customer. Never say it is
            confirmed, paid, or available. Do not promise a date or availability.
            For property/vendor details, summarize only verified description, capacity, price,
            location, and amenities. For off-topic questions, answer briefly and steer back to
            BookFarmVilla venues or vendors.
            Remember conversation history and preserve stated requirements.
            Keep replies short and conversational; use a list only when it improves options.
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
        log.info("AI intent extraction completed in {} ms", elapsedMillis(extractionStartedAt));
        AiRequirements requirements = mergeRequirements(request.message(), extracted.requirements());

        long searchStartedAt = System.nanoTime();
        boolean isVendor = "SEARCH_VENDOR".equalsIgnoreCase(extracted.intent());
        AiPropertySearchService.PropertySearchResult searchResult = isVendor
                ? new AiPropertySearchService.PropertySearchResult(List.of(), List.of())
                : searchService.searchPropertiesWithAlternatives(
                        requirements, request.latitude(), request.longitude());
        List<PropertyResponse> properties = searchResult.properties();
        AiPropertySearchService.VendorSearchResult vendorSearchResult = isVendor
                ? searchService.searchVendorsWithAlternatives(requirements)
                : new AiPropertySearchService.VendorSearchResult(List.of(), List.of());
        List<VendorResponse> vendors = vendorSearchResult.vendors();
        List<String> relaxedFilters = new ArrayList<>(searchResult.relaxedFilters());
        relaxedFilters.addAll(vendorSearchResult.relaxedFilters());

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
        log.info("AI listing search completed in {} ms", elapsedMillis(searchStartedAt));

        String verifiedData = buildVerifiedData(properties, vendors);
        String prompt = buildResponsePrompt(
                request, requirements, properties, vendors, verifiedData, relaxedFilters);

        String message;
        long responseStartedAt = System.nanoTime();
        try {
            message = providerRouter.current().generate(RESPONSE_SYSTEM, prompt);
        } catch (AiProviderException ex) {
            log.warn("AI response generation failed; using verified-data fallback");
            message = fallbackMessage(request.message(), requirements, properties, vendors, relaxedFilters);
        }
        log.info("AI response generation completed in {} ms", elapsedMillis(responseStartedAt));

        boolean nearMeRequested = containsNearMe(request.message());
        boolean coordinatesSupplied = request.latitude() != null && request.longitude() != null;
        boolean coordinatesAvailable = properties.stream()
                .anyMatch(property -> property.latitude() != null && property.longitude() != null);
        boolean locationSupported = coordinatesSupplied && coordinatesAvailable;
        String locationMessage = null;

        if (nearMeRequested) {
            if (!coordinatesSupplied) {
                locationMessage = "Please allow location access to sort verified nearby listings.";
            } else if (!coordinatesAvailable) {
                locationMessage = "Nearby sorting is unavailable because these listings do not have verified coordinates.";
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
            log.warn("AI intent extraction failed; using deterministic fallback");
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

    private AiRequirements mergeRequirements(String message, AiRequirements current) {
        AiRequirements latest = current == null ? AiRequirements.empty() : current;
        String marker = "Previously understood requirements: ";
        int markerIndex = message.indexOf(marker);
        if (markerIndex < 0) return latest;

        int start = markerIndex + marker.length();
        int end = message.indexOf('\n', start);
        String priorJson = message.substring(start, end < 0 ? message.length() : end).trim();
        try {
            AiRequirements prior = objectMapper.readValue(priorJson, AiRequirements.class);
            return new AiRequirements(
                    firstNonNull(latest.location(), prior.location()),
                    firstNonNull(latest.guests(), prior.guests()),
                    firstNonNull(latest.budget(), prior.budget()),
                    firstNonNull(latest.propertyType(), prior.propertyType()),
                    latest.amenities() == null || latest.amenities().isEmpty()
                            ? prior.amenities()
                            : latest.amenities(),
                    firstNonNull(latest.purpose(), prior.purpose()),
                    firstNonNull(latest.date(), prior.date())
            );
        } catch (Exception ex) {
            log.warn("Previously understood AI requirements could not be parsed");
            return latest;
        }
    }

    private <T> T firstNonNull(T current, T previous) {
        return current != null ? current : previous;
    }

    private String buildResponsePrompt(
            AiChatRequest request,
            AiRequirements requirements,
            List<PropertyResponse> properties,
            List<VendorResponse> vendors,
            String verifiedData,
            List<String> relaxedFilters
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

                FILTERS_RELAXED:
                %s

                Rules:
                - The UI renders property/vendor cards from VERIFIED_DATA; never invent cards or their details.
                - Ask for one missing requirement at a time, and only when needed to refine results.
                - If VERIFIED_DATA is empty, say no exact verified match was found, then invite one useful refinement.
                - If FILTERS_RELAXED is non-empty, explain every relaxed filter plainly and recommend these real cards.
                - If the customer asks about a field absent from VERIFIED_DATA, say it is not verified.
                - Do not answer unrelated questions at length; steer back to venues or vendors.
                """.formatted(
                renderHistory(request.history()),
                request.message(),
                toJson(requirements),
                verifiedData,
                relaxedFilters.isEmpty() ? "none" : String.join(", ", relaxedFilters)
        );
    }

    private String buildVerifiedData(List<PropertyResponse> properties, List<VendorResponse> vendors) {
        List<Object> safe = new ArrayList<>();
        safe.addAll(properties.stream().map(p -> new VerifiedProperty(
                p.id(), p.title(), p.description(), p.city(), p.type() == null ? null : p.type().name(),
                formatPriceRange(p.startingPrice(), p.endingPrice()), p.capacity(), p.amenities(), p.averageRating(),
                p.address(), p.contactPhone(), p.images().stream().findFirst().map(i -> i.url()).orElse(null)
        )).toList());
        safe.addAll(vendors.stream().map(v -> new VerifiedVendor(
                v.id(), v.name(), v.description(), v.city(), v.category() == null ? null : v.category().name(),
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
                || m.contains("photographer") || m.contains("dj") || m.contains("makeup artist");
        Integer guests = extractNumberBefore(m, "people", "person", "guests", "guest", "log");
        Integer budget = extractBudget(m);

        String type = m.contains("villa") ? "VILLA"
                : m.contains("wedding lawn") || m.contains("lawn") ? "WEDDING_LAWN"
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
        String normalized = text.replace(",", "");
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:under|below|less than|within|up to|budget(?:\\s+is)?|₹|rs\\.?|inr)\\s*₹?\\s*(\\d+(?:\\.\\d+)?)\\s*(k|thousand|lakh|lac)?|\\b(\\d+(?:\\.\\d+)?)\\s*(k|thousand|lakh|lac)\\b|\\b(\\d+(?:\\.\\d+)?)\\s*(?:rupees?|rs\\.?|budget)\\b",
                        java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(normalized);
        if (!m.find()) return null;
        String number = m.group(1) != null ? m.group(1)
                : m.group(3) != null ? m.group(3) : m.group(5);
        String unit = m.group(2) != null ? m.group(2)
                : m.group(4) != null ? m.group(4) : "";
        double value = Double.parseDouble(number);
        if ("k".equalsIgnoreCase(unit) || "thousand".equalsIgnoreCase(unit)) value *= 1000;
        if ("lakh".equalsIgnoreCase(unit) || "lac".equalsIgnoreCase(unit)) value *= 100000;
        return (int) value;
    }

    private String fallbackMessage(
            String userMessage,
            AiRequirements r,
            List<PropertyResponse> properties,
            List<VendorResponse> vendors,
            List<String> relaxedFilters
    ) {
        boolean hinglish = isHinglish(userMessage);
        String relaxed = relaxedFilters.isEmpty()
                ? ""
                : hinglish
                        ? " Exact match nahi mila, isliye " + relaxedFilters.get(0) + " filter relax kiya hai."
                        : " No exact match, so I relaxed the " + relaxedFilters.get(0) + " filter.";
        if (!properties.isEmpty()) {
            return hinglish
                    ? "Aapke liye " + properties.size() + " verified property options hain." + relaxed
                    : "I found " + properties.size() + " verified property options." + relaxed;
        }
        if (!vendors.isEmpty()) {
            return hinglish
                    ? "Aapke liye " + vendors.size() + " verified vendor options hain." + relaxed
                    : "I found " + vendors.size() + " verified vendor options." + relaxed;
        }
        if (r.location() == null && r.guests() == null && r.budget() == null && r.propertyType() == null) {
            return hinglish
                    ? "Aapko **farmhouse**, **villa**, ya **wedding lawn** chahiye? Kaunsi **location**, **budget**, aur **guest count** rahega?"
                    : "What **venue type**, **location**, **budget**, and **guest count** should I use?";
        }
        return hinglish
                ? "Is request ka exact verified match nahi mila. Kya aap **budget** ya **location** thodi flexible rakh sakte hain?"
                : "I couldn’t find an exact verified match. Could you be flexible on **budget** or **location**?";
    }

    static boolean isHinglish(String message) {
        if (message == null) return false;
        return message.toLowerCase(Locale.ROOT).matches(
                "(?s).*\\b(mujhe|chahiye|dikhao|dikha|kuch|aur|nahi|nahin|pasand|kaunsa|kaisi|hai|hain|haan|han|mera|meri|aapke|liye|karo|krna|karna|bhejo|bataye|batao|kr|kya|acha|accha|sahi|kitna|kitne|mein|wala|wali|kar do|kr do)\\b.*"
        );
    }

    private record VerifiedProperty(
            String id, String title, String description, String city, String type, String priceRange,
            Integer capacity, java.util.Set<String> amenities, Double rating,
            String address, String contactPhone, String imageUrl
    ) {}

    private record VerifiedVendor(
            String id, String name, String description, String city, String category, String priceRange,
            Double rating, String contactPhone, String imageUrl
    ) {}
}
