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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiPropertySearchService {

    private static final Pattern MONEY = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(k|K|lakh|lac|L)?");

    private final PropertyRepository propertyRepository;
    private final VendorRepository vendorRepository;
    private final ReviewRepository reviewRepository;

    public AiPropertySearchService(
            PropertyRepository propertyRepository,
            VendorRepository vendorRepository,
            ReviewRepository reviewRepository
    ) {
        this.propertyRepository = propertyRepository;
        this.vendorRepository = vendorRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<PropertyResponse> searchProperties(AiRequirements r) {
        Specification<Property> spec = Specification
                .where(hasStatus(ListingStatus.ACTIVE))
                .and(hasCity(r.location()))
                .and(hasCapacity(r.guests()))
                .and(hasType(parsePropertyType(r.propertyType())));

        List<Property> candidates = propertyRepository.findAll(spec);

        if (r.amenities() != null && !r.amenities().isEmpty()) {
            List<String> wanted = r.amenities().stream()
                    .filter(Objects::nonNull)
                    .map(this::normalise)
                    .toList();

            candidates = candidates.stream()
                    .filter(p -> {
                        Set<String> actual = p.getAmenities().stream()
                                .map(a -> normalise(a.getName()))
                                .collect(java.util.stream.Collectors.toSet());
                        return wanted.stream().allMatch(w -> actual.stream().anyMatch(a -> a.contains(w) || w.contains(a)));
                    })
                    .toList();
        }

        if (r.budget() != null) {
            candidates = candidates.stream()
                    .filter(p -> priceFitsBudget(p.getPriceRange(), r.budget()))
                    .toList();
        }

        return candidates.stream()
                .limit(8)
                .map(this::toPropertyResponseForAi)
                .toList();
    }

    public List<VendorResponse> searchVendors(AiRequirements r) {
        Specification<Vendor> spec = Specification
                .where(hasVendorStatus(ListingStatus.ACTIVE))
                .and(hasVendorCity(r.location()))
                .and(hasVendorCategory(parseVendorCategory(r.propertyType())));

        return vendorRepository.findAll(spec).stream()
                .limit(8)
                .map(this::toVendorResponseForAi)
                .toList();
    }

    private boolean priceFitsBudget(String priceRange, int budget) {
        if (priceRange == null || priceRange.isBlank()) return true;
        Matcher matcher = MONEY.matcher(priceRange.replace(",", ""));
        List<Integer> values = new ArrayList<>();
        while (matcher.find()) {
            double number = Double.parseDouble(matcher.group(1));
            String suffix = matcher.group(2);
            if (suffix != null && suffix.equalsIgnoreCase("k")) number *= 1000;
            if (suffix != null && (suffix.equalsIgnoreCase("lakh") || suffix.equalsIgnoreCase("lac"))) number *= 100000;
            values.add((int) number);
        }
        if (values.isEmpty()) return true;
        return Collections.max(values) <= budget;
    }

    private PropertyType parsePropertyType(String value) {
        if (value == null) return null;
        return switch (value.trim().toUpperCase().replace(" ", "_")) {
            case "FARMHOUSE", "FARM_HOUSE" -> PropertyType.FARMHOUSE;
            case "VILLA" -> PropertyType.VILLA;
            case "WEDDING_LAWN", "WEDDING_LAWNS", "LAWN" -> PropertyType.WEDDING_LAWN;
            default -> null;
        };
    }

    private VendorCategory parseVendorCategory(String value) {
        if (value == null) return null;
        try {
            return VendorCategory.valueOf(value.trim().toUpperCase().replace(" ", "_"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private Specification<Property> hasStatus(ListingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<Property> hasCity(String city) {
        if (city == null || city.isBlank()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("city")), city.toLowerCase());
    }

    private Specification<Property> hasCapacity(Integer guests) {
        if (guests == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("capacity"), guests);
    }

    private Specification<Property> hasType(PropertyType type) {
        if (type == null) return null;
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    private Specification<Vendor> hasVendorStatus(ListingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<Vendor> hasVendorCity(String city) {
        if (city == null || city.isBlank()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("city")), city.toLowerCase());
    }

    private Specification<Vendor> hasVendorCategory(VendorCategory category) {
        if (category == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    private String normalise(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    public PropertyResponse toPropertyResponseForAi(Property p) {
        List<PropertyImageResponse> images = p.getImages().stream()
                .map(i -> new PropertyImageResponse(i.getId(), i.getUrl(), i.isPrimary()))
                .toList();

        Double rating = reviewRepository.findByPropertyId(p.getId()).stream()
                .mapToInt(r -> r.getRating())
                .average()
                .orElse(0);

        Set<String> amenities = p.getAmenities().stream().map(a -> a.getName()).collect(java.util.stream.Collectors.toSet());

        return new PropertyResponse(
                p.getId(), p.getTitle(), p.getDescription(), p.getType(), p.getAddress(), p.getCity(),
                p.getPriceRange(), p.getCapacity(), p.getContactPhone(), p.getStatus(), amenities,
                images, rating, p.getCreatedAt(), p.getModifiedAt()
        );
    }

    public VendorResponse toVendorResponseForAi(Vendor v) {
        List<VendorImageResponse> images = v.getImages().stream()
                .map(i -> new VendorImageResponse(i.getId(), i.getUrl(), i.isPrimary()))
                .toList();

        Double rating = reviewRepository.findByVendorId(v.getId()).stream()
                .mapToInt(r -> r.getRating())
                .average()
                .orElse(0);

        return new VendorResponse(
                v.getId(), v.getName(), v.getCategory(), v.getDescription(), v.getCity(),
                v.getPriceRange(), v.getContactPhone(), v.getStatus(), images, rating,
                v.getCreatedAt(), v.getModifiedAt()
        );
    }
}
