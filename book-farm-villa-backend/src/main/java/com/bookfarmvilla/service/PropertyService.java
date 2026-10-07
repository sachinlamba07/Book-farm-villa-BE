package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.PropertyRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PropertyImageResponse;
import com.bookfarmvilla.dto.response.PropertyResponse;
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
import com.bookfarmvilla.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final AmenityRepository amenityRepository;
    private final ReviewRepository reviewRepository;

    public PropertyService(PropertyRepository propertyRepository, AmenityRepository amenityRepository, ReviewRepository reviewRepository) {
        this.propertyRepository = propertyRepository;
        this.amenityRepository = amenityRepository;
        this.reviewRepository = reviewRepository;
    }

    public PageResponse<PropertyResponse> search(
            String city, PropertyType type, ListingStatus status, Integer minCapacity, String keyword, Pageable pageable
    ) {
        Specification<Property> spec = Specification.where(PropertySpecifications.hasCity(city))
                .and(PropertySpecifications.hasType(type))
                .and(PropertySpecifications.hasStatus(status))
                .and(PropertySpecifications.minCapacity(minCapacity))
                .and(PropertySpecifications.titleContains(keyword));

        Page<Property> page = propertyRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    public PropertyResponse findById(String id) {
        Property property = getOrThrow(id);
        return toResponse(property);
    }

    @Transactional
    public PropertyResponse create(PropertyRequest request) {
        Property property = new Property();
        property.setId(request.id()); // preserved if provided, generated in BaseEntity otherwise
        applyRequest(property, request);
        return toResponse(propertyRepository.save(property));
    }

    @Transactional
    public PropertyResponse update(String id, PropertyRequest request) {
        Property property = getOrThrow(id);
        applyRequest(property, request);
        return toResponse(propertyRepository.save(property));
    }

    @Transactional
    public void delete(String id) {
        if (!propertyRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Property", id);
        }
        propertyRepository.deleteById(id);
    }

    private void applyRequest(Property property, PropertyRequest request) {
        property.setTitle(request.title());
        property.setDescription(request.description());
        property.setType(request.type());
        property.setAddress(request.address());
        property.setCity(request.city());
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
                .orElseThrow(() -> ResourceNotFoundException.of("Property", id));
    }

    private PropertyResponse toResponse(Property p) {
        List<PropertyImageResponse> images = p.getImages().stream()
                .map(img -> new PropertyImageResponse(img.getId(), img.getUrl(), img.isPrimary()))
                .toList();

        Set<String> amenityNames = p.getAmenities().stream()
                .map(Amenity::getName)
                .collect(Collectors.toSet());

        java.util.OptionalDouble avgOpt = reviewRepository.findByPropertyId(p.getId()).stream()
                .mapToInt(r -> r.getRating())
                .average();
        Double avgRating = avgOpt.isPresent() ? avgOpt.getAsDouble() : null;

        return new PropertyResponse(
                p.getId(), p.getTitle(), p.getDescription(), p.getType(), p.getAddress(), p.getCity(),
                p.getLatitude(), p.getLongitude(), p.getStartingPrice(), p.getEndingPrice(),
                p.getCapacity(), p.getContactPhone(), p.getStatus(),
                amenityNames, images, avgRating, p.getCreatedAt(), p.getModifiedAt()
        );
    }
}
