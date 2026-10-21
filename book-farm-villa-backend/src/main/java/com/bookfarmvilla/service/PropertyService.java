package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.PropertyRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PropertyImageResponse;
import com.bookfarmvilla.dto.response.PropertyResponse;
import com.bookfarmvilla.dto.response.PropertyV2Response;
import com.bookfarmvilla.entity.Amenity;
import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.PropertyImage;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.exception.BadRequestException;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.AmenityRepository;
import com.bookfarmvilla.repository.PropertyRepository;
import com.bookfarmvilla.repository.PropertySpecifications;
import com.bookfarmvilla.util.PropertySlug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private static final int MAX_PAGE_SIZE = 100;

    private final PropertyRepository propertyRepository;
    private final AmenityRepository amenityRepository;

    public PropertyService(PropertyRepository propertyRepository, AmenityRepository amenityRepository) {
        this.propertyRepository = propertyRepository;
        this.amenityRepository = amenityRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "propertySearch", key = "T(java.util.Objects).toString(#city, '') + '|' + T(java.util.Objects).toString(#type, '') + '|' + T(java.util.Objects).toString(#status, '') + '|' + T(java.util.Objects).toString(#minCapacity, '') + '|' + T(java.util.Objects).toString(#keyword, '') + '|' + #pageable.toString()")
    public PageResponse<PropertyResponse> search(
            String city, PropertyType type, ListingStatus status, Integer minCapacity, String keyword, Pageable pageable
    ) {
        pageable = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE), pageable.getSort());
        Specification<Property> spec = Specification.where(PropertySpecifications.hasCity(city))
                .and(PropertySpecifications.hasType(type))
                .and(PropertySpecifications.hasStatus(status))
                .and(PropertySpecifications.minCapacity(minCapacity))
                .and(PropertySpecifications.titleContains(keyword));

        Page<Property> page = propertyRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "propertyV2Search", key = "T(java.util.Objects).toString(#city, '') + '|' + T(java.util.Objects).toString(#type, '') + '|' + T(java.util.Objects).toString(#status, '') + '|' + T(java.util.Objects).toString(#minCapacity, '') + '|' + T(java.util.Objects).toString(#keyword, '') + '|' + #pageable.toString()")
    public PageResponse<PropertyV2Response> searchV2(
            String city, PropertyType type, ListingStatus status, Integer minCapacity, String keyword, Pageable pageable
    ) {
        pageable = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE), pageable.getSort());
        Specification<Property> spec = Specification.where(PropertySpecifications.hasCity(city))
                .and(PropertySpecifications.hasType(type))
                .and(PropertySpecifications.hasStatus(status))
                .and(PropertySpecifications.minCapacity(minCapacity))
                .and(PropertySpecifications.titleContains(keyword));

        Page<Property> page = propertyRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(this::toV2Response));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "propertyDetails", key = "#id")
    public PropertyResponse findById(String id) {
        Property property = getOrThrow(id);
        return toResponse(property);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "propertyV2Details", key = "#id")
    public PropertyV2Response findByIdV2(String id) {
        Property property = getOrThrow(id);
        return toV2Response(property);
    }

    @Transactional
    @CacheEvict(cacheNames = {"propertySearch", "propertyV2Search", "propertyCards", "propertyDetails", "propertyV2Details"}, allEntries = true)
    public PropertyResponse create(PropertyRequest request) {
        Property property = new Property();
        property.setId(request.id()); // preserved if provided, generated in BaseEntity otherwise
        applyRequest(property, request);
        return toResponse(propertyRepository.save(property));
    }

    @Transactional
    @CacheEvict(cacheNames = {"propertySearch", "propertyV2Search", "propertyCards", "propertyDetails", "propertyV2Details"}, allEntries = true)
    public PropertyResponse update(String id, PropertyRequest request) {
        Property property = getOrThrow(id);
        applyRequest(property, request);
        return toResponse(propertyRepository.save(property));
    }

    @Transactional
    @CacheEvict(cacheNames = {"propertySearch", "propertyV2Search", "propertyCards", "propertyDetails", "propertyV2Details"}, allEntries = true)
    public void delete(String id) {
        propertyRepository.delete(getOrThrow(id));
    }

    private void applyRequest(Property property, PropertyRequest request) {
        property.setTitle(request.title());
        property.setCity(request.city());
        property.setSlug(resolveSlug(property, request.slug()));
        property.setOriginalPropertyName(request.originalPropertyName());
        property.setDescription(request.description());
        property.setType(request.type());
        property.setAddress(request.address());
        property.setLatitude(request.latitude());
        property.setLongitude(request.longitude());
        property.setStartingPrice(request.startingPrice());
        property.setEndingPrice(request.endingPrice());
        property.setCapacity(request.capacity());
        property.setContactPhone(request.contactPhone());
        property.setStatus(request.status() != null ? request.status() : ListingStatus.ACTIVE);

        if (request.amenityIds() != null || request.amenities() != null) {
            Set<Amenity> amenities = new HashSet<>();
            if (request.amenityIds() != null) {
                Set<Amenity> amenitiesById = new HashSet<>(amenityRepository.findAllById(request.amenityIds()));
                if (amenitiesById.size() != request.amenityIds().size()) {
                    throw new BadRequestException("One or more amenity IDs are invalid");
                }
                amenities.addAll(amenitiesById);
            }

            if (request.amenities() != null) {
                for (String name : request.amenities()) {
                    String normalizedName = name.trim();
                    Amenity amenity = amenityRepository.findByNameIgnoreCase(normalizedName)
                            .orElseGet(() -> {
                                Amenity newAmenity = new Amenity();
                                newAmenity.setName(normalizedName);
                                return amenityRepository.save(newAmenity);
                            });
                    amenities.add(amenity);
                }
            }

            property.setAmenities(amenities);
        }

        if (request.imageUrls() != null) {
            property.getImages().clear();
            for (int i = 0; i < request.imageUrls().size(); i++) {
                PropertyImage image = new PropertyImage();
                image.setProperty(property);
                image.setUrl(request.imageUrls().get(i));
                image.setPrimary(i == 0);
                property.getImages().add(image);
            }
        }

    }

    private Property getOrThrow(String id) {
        return propertyRepository.findById(id)
                .or(() -> propertyRepository.findBySlug(id))
                .orElseThrow(() -> ResourceNotFoundException.of("Property", id));
    }

    private String resolveSlug(Property property, String requestedSlug) {
        boolean generated = requestedSlug == null || requestedSlug.isBlank();
        String baseSlug = generated
                ? PropertySlug.from(property.getTitle(), property.getCity())
                : requestedSlug.trim();
        String slug = baseSlug;
        int suffix = 2;
        while (true) {
            Property existing = propertyRepository.findBySlug(slug).orElse(null);
            if (existing == null || existing.getId().equals(property.getId())) {
                return slug;
            }
            if (!generated) {
                throw new BadRequestException("Property slug is already in use");
            }
            slug = PropertySlug.withSuffix(baseSlug, suffix++);
        }
    }

    private PropertyResponse toResponse(Property p) {
        List<PropertyImageResponse> images = p.getImages().stream()
                .map(img -> new PropertyImageResponse(img.getId(), img.getUrl(), img.isPrimary()))
                .toList();

        Set<String> amenityNames = p.getAmenities().stream()
                .map(Amenity::getName)
                .collect(Collectors.toSet());

        Double avgRating = p.getRatingCount() != null && p.getRatingCount() > 0
                ? p.getRatingAverage() : null;

        return new PropertyResponse(
                p.getId(), p.getSlug(), p.getTitle(), p.getDescription(), p.getType(), p.getAddress(), p.getCity(),
                p.getLatitude(), p.getLongitude(), p.getStartingPrice(), p.getEndingPrice(),
                p.getCapacity(), p.getContactPhone(), p.getStatus(),
                amenityNames, images, avgRating, p.getCreatedAt(), p.getModifiedAt()
        );
    }

    private PropertyV2Response toV2Response(Property property) {
        PropertyResponse response = toResponse(property);
        return new PropertyV2Response(
                response.id(), response.slug(), response.title(), property.getOriginalPropertyName(), response.description(),
                response.type(), response.address(), response.city(), response.latitude(), response.longitude(),
                response.startingPrice(), response.endingPrice(), response.capacity(), response.contactPhone(),
                response.status(), response.amenities(), response.images(), response.averageRating(),
                response.createdAt(), response.modifiedAt()
        );
    }
}
