package com.bookfarmvilla.ai;

import com.bookfarmvilla.dto.response.PropertyImageResponse;
import com.bookfarmvilla.dto.response.PropertyResponse;
import com.bookfarmvilla.dto.response.VendorImageResponse;
import com.bookfarmvilla.dto.response.VendorResponse;
import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.Vendor;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.entity.enums.VendorCategory;
import com.bookfarmvilla.repository.PropertyRepository;
import com.bookfarmvilla.repository.VendorRepository;
import com.bookfarmvilla.repository.ReviewRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiPropertySearchService {

    private final PropertyRepository propertyRepository;
    private final VendorRepository vendorRepository;
    private final ReviewRepository reviewRepository;

    public AiPropertySearchService(
            PropertyRepository propertyRepository,
            VendorRepository vendorRepository,
            ReviewRepository reviewRepository) {
        this.propertyRepository = propertyRepository;
        this.vendorRepository = vendorRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<PropertyResponse> searchProperties(AiRequirements r) {
        return searchPropertiesWithAlternatives(r, null, null).properties();
    }

    @Transactional(readOnly = true)
    public PropertySearchResult searchPropertiesWithAlternatives(
            AiRequirements requirements,
            Double latitude,
            Double longitude) {
        PropertyType requestedType = parsePropertyType(requirements.propertyType());
        List<String> relaxedFilters = new ArrayList<>();
        List<Property> candidates = findProperties(
                requirements.location(), requirements.guests(), requestedType);
        List<Property> matches = filterProperties(candidates, requirements, true);

        if (matches.isEmpty() && requirements.budget() != null) {
            matches = filterProperties(candidates, requirements, false);
            if (!matches.isEmpty()) {
                relaxedFilters.add("budget");
            }
        }

        if (matches.isEmpty() && hasText(requirements.location())) {
            candidates = findProperties(null, requirements.guests(), requestedType);
            matches = filterProperties(candidates, requirements, false);
            if (!matches.isEmpty()) {
                if (requirements.budget() != null) {
                    relaxedFilters.add("budget");
                }
                relaxedFilters.add("location");
            }
        }

        if (matches.isEmpty() && requestedType != null) {
            candidates = findProperties(null, requirements.guests(), null);
            matches = filterProperties(candidates, requirements, false);
            if (!matches.isEmpty()) {
                if (requirements.budget() != null) {
                    relaxedFilters.add("budget");
                }
                if (hasText(requirements.location())) {
                    relaxedFilters.add("location");
                }
                relaxedFilters.add("property type");
            }
        }

        Comparator<Property> priceOrder = Comparator.comparing(
                (Property property) -> property.getStartingPrice() != null
                        ? property.getStartingPrice()
                        : property.getEndingPrice(),
                Comparator.nullsLast(Comparator.naturalOrder()));
        if (requirements.budget() != null && !relaxedFilters.isEmpty()) {
            priceOrder = Comparator.comparing(
                    (Property property) -> distanceFromBudget(property, requirements.budget()),
                    Comparator.nullsLast(Comparator.naturalOrder()));
        }

        Comparator<Property> order = priceOrder;
        if (latitude != null && longitude != null) {
            Comparator<Property> distanceOrder = Comparator.comparing(
                    (Property property) -> distanceKm(latitude, longitude, property),
                    Comparator.nullsLast(Comparator.naturalOrder()));
            order = distanceOrder.thenComparing(priceOrder);
        }

        List<PropertyResponse> properties = matches.stream()
                .sorted(order)
                .limit(8)
                .map(this::toPropertyResponseForAi)
                .toList();
        return new PropertySearchResult(properties, List.copyOf(relaxedFilters));
    }

    private List<Property> findProperties(String location, Integer guests, PropertyType type) {
        Specification<Property> spec = Specification
                .where(hasStatus(ListingStatus.ACTIVE))
                .and(hasLocation(location))
                .and(hasCapacity(guests))
                .and(hasType(type));
        // Bound candidates to avoid loading the entire catalog into memory during an AI
        // request.
        return propertyRepository.findAll(spec, PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
    }

    private List<Property> filterProperties(
            List<Property> candidates,
            AiRequirements requirements,
            boolean enforceBudget) {
        List<String> wantedAmenities = requirements.amenities() == null
                ? List.of()
                : requirements.amenities().stream()
                        .filter(Objects::nonNull)
                        .map(this::normalise)
                        .toList();
        return candidates.stream()
                .filter(property -> matchesAmenities(property, wantedAmenities))
                .filter(property -> !enforceBudget || requirements.budget() == null
                        || priceFitsBudget(
                                property.getStartingPrice(),
                                property.getEndingPrice(),
                                requirements.budget()))
                .toList();
    }

    private boolean matchesAmenities(Property property, List<String> wanted) {
        if (wanted.isEmpty())
            return true;
        Set<String> actual = property.getAmenities().stream()
                .map(amenity -> normalise(amenity.getName()))
                .collect(java.util.stream.Collectors.toSet());
        return wanted.stream()
                .allMatch(required -> actual.stream()
                        .anyMatch(value -> value.contains(required) || required.contains(value)));
    }

    private java.math.BigDecimal distanceFromBudget(Property property, int budget) {
        java.math.BigDecimal price = property.getStartingPrice() != null
                ? property.getStartingPrice()
                : property.getEndingPrice();
        return price == null
                ? null
                : price.subtract(java.math.BigDecimal.valueOf(budget)).abs();
    }

    private Double distanceKm(double latitude, double longitude, Property property) {
        if (property.getLatitude() == null || property.getLongitude() == null)
            return null;
        double latitudeDelta = Math.toRadians(property.getLatitude() - latitude);
        double longitudeDelta = Math.toRadians(property.getLongitude() - longitude);
        double a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
                + Math.cos(Math.toRadians(latitude))
                        * Math.cos(Math.toRadians(property.getLatitude()))
                        * Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public List<VendorResponse> searchVendors(AiRequirements r) {
        return searchVendorsWithAlternatives(r).vendors();
    }

    @Transactional(readOnly = true)
    public VendorSearchResult searchVendorsWithAlternatives(AiRequirements r) {
        VendorCategory category = parseVendorCategory(r.propertyType());
        List<String> relaxedFilters = new ArrayList<>();
        List<Vendor> candidates = findVendors(r.location(), category);
        if (candidates.isEmpty() && hasText(r.location())) {
            candidates = findVendors(null, category);
            if (!candidates.isEmpty()) {
                relaxedFilters.add("location");
            }
        }
        if (candidates.isEmpty() && category != null) {
            candidates = findVendors(null, null);
            if (!candidates.isEmpty()) {
                if (hasText(r.location())) {
                    relaxedFilters.add("location");
                }
                relaxedFilters.add("vendor type");
            }
        }
        List<VendorResponse> vendors = candidates.stream()
                .limit(8)
                .map(this::toVendorResponseForAi)
                .toList();
        return new VendorSearchResult(vendors, List.copyOf(relaxedFilters));
    }

    private List<Vendor> findVendors(String location, VendorCategory category) {
        Specification<Vendor> spec = Specification
                .where(hasVendorStatus(ListingStatus.ACTIVE))
                .and(hasVendorCity(location))
                .and(hasVendorCategory(category));
        return vendorRepository.findAll(spec, PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
    }

    private PropertyType parsePropertyType(String value) {
        if (value == null)
            return null;
        return switch (value.trim().toUpperCase().replace(" ", "_")) {
            case "FARMHOUSE", "FARM_HOUSE" -> PropertyType.FARMHOUSE;
            case "VILLA" -> PropertyType.VILLA;
            case "WEDDING_LAWN", "WEDDING_LAWNS", "LAWN" -> PropertyType.WEDDING_LAWN;
            default -> null;
        };
    }

    private VendorCategory parseVendorCategory(String value) {
        if (value == null)
            return null;
        try {
            return VendorCategory.valueOf(value.trim().toUpperCase().replace(" ", "_"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private Specification<Property> hasStatus(ListingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<Property> hasLocation(String location) {
        if (!hasText(location))
            return null;
        List<String> terms = Arrays.stream(location.toLowerCase(Locale.ROOT).trim().split("\\s+"))
                .filter(term -> !term.isBlank())
                .toList();
        return (root, criteriaQuery, cb) -> cb.and(terms.stream()
                .map(term -> {
                    String query = "%" + term + "%";
                    return cb.or(
                            cb.like(cb.lower(root.get("city")), query),
                            cb.like(cb.lower(root.get("address")), query));
                })
                .toArray(jakarta.persistence.criteria.Predicate[]::new));
    }

    private Specification<Property> hasCapacity(Integer guests) {
        if (guests == null)
            return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("capacity"), guests);
    }

    private Specification<Property> hasType(PropertyType type) {
        if (type == null)
            return null;
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    private Specification<Vendor> hasVendorStatus(ListingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<Vendor> hasVendorCity(String city) {
        if (city == null || city.isBlank())
            return null;
        String query = "%" + city.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, criteriaQuery, cb) -> cb.like(cb.lower(root.get("city")), query);
    }

    private Specification<Vendor> hasVendorCategory(VendorCategory category) {
        if (category == null)
            return null;
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    private String normalise(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean priceFitsBudget(
            java.math.BigDecimal startingPrice,
            java.math.BigDecimal endingPrice,
            int budget) {
        java.math.BigDecimal maximumKnownPrice = endingPrice != null ? endingPrice : startingPrice;
        if (startingPrice != null) {
            return startingPrice.compareTo(java.math.BigDecimal.valueOf(budget)) <= 0;
        }
        return maximumKnownPrice != null
                && maximumKnownPrice.compareTo(java.math.BigDecimal.valueOf(budget)) <= 0;
    }

    public record PropertySearchResult(
            List<PropertyResponse> properties,
            List<String> relaxedFilters) {
    }

    public record VendorSearchResult(
            List<VendorResponse> vendors,
            List<String> relaxedFilters) {
    }

    public PropertyResponse toPropertyResponseForAi(Property p) {
        List<PropertyImageResponse> images = p.getImages().stream()
                .map(i -> new PropertyImageResponse(i.getId(), i.getUrl(), i.isPrimary()))
                .toList();

        // Stored aggregate (V9 trigger) instead of loading every review.
        Double rating = p.getRatingCount() != null && p.getRatingCount() > 0 ? p.getRatingAverage() : null;

        Set<String> amenities = p.getAmenities().stream()
                .map(a -> a.getName())
                .collect(java.util.stream.Collectors.toSet());

        // contactPhone is deliberately null: AI responses are public.
        return new PropertyResponse(
                p.getId(), p.getSlug(), p.getTitle(), p.getDescription(), p.getType(), p.getAddress(), p.getCity(),
                p.getLatitude(), p.getLongitude(), p.getStartingPrice(), p.getEndingPrice(),
                p.getCapacity(), null, p.getStatus(), amenities,
                images, rating, p.getCreatedAt(), p.getModifiedAt());
    }

    public VendorResponse toVendorResponseForAi(Vendor v) {
        List<VendorImageResponse> images = v.getImages().stream()
                .map(i -> new VendorImageResponse(i.getId(), i.getUrl(), i.isPrimary()))
                .toList();

        Double rating = v.getRatingCount() != null && v.getRatingCount() > 0 ? v.getRatingAverage() : null;

        // contactPhone is deliberately null: AI responses are public.
        return new VendorResponse(
                v.getId(), v.getSlug(), v.getName(), v.getCategory(), v.getDescription(), v.getCity(),
                v.getPriceRange(), null, v.getStatus(), images, rating,
                v.getCreatedAt(), v.getModifiedAt());
    }
}
