package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PropertyCardAmenityRow;
import com.bookfarmvilla.dto.response.PropertyCardBaseRow;
import com.bookfarmvilla.dto.response.PropertyCardImageRow;
import com.bookfarmvilla.dto.response.PropertyCardResponse;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.exception.BadRequestException;
import com.bookfarmvilla.repository.PropertyCardQueryRepository;
import com.bookfarmvilla.repository.PropertyImageRepository;
import com.bookfarmvilla.repository.PropertyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PropertyCardService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SORT_FIELDS = Set.of("title", "city", "startingPrice", "capacity", "createdAt");

    private final PropertyCardQueryRepository propertyCardQueryRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;

    public PropertyCardService(
            PropertyCardQueryRepository propertyCardQueryRepository,
            PropertyRepository propertyRepository,
            PropertyImageRepository propertyImageRepository
    ) {
        this.propertyCardQueryRepository = propertyCardQueryRepository;
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "propertyCards", key = "T(java.util.Objects).toString(#types, '') + '|' + T(java.util.Objects).toString(#city, '') + '|' + T(java.util.Objects).toString(#keyword, '') + '|' + T(java.util.Objects).toString(#minCapacity, '') + '|' + #pageable.toString()")
    public PageResponse<PropertyCardResponse> findCards(
            Set<PropertyType> types,
            String city,
            String keyword,
            Integer minCapacity,
            Pageable pageable
    ) {
        pageable.getSort().forEach(order -> {
            if (!SORT_FIELDS.contains(order.getProperty())) {
                throw new BadRequestException("Unsupported property-card sort field: " + order.getProperty());
            }
        });
        Pageable boundedPageable = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
                pageable.getSort()
        );
        Set<PropertyType> effectiveTypes = (types == null || types.isEmpty())
                ? Set.of(PropertyType.values()) : types;
        Page<PropertyCardBaseRow> page = propertyCardQueryRepository.findCards(
                effectiveTypes,
                city == null || city.isBlank() ? null : city.trim(),
                keyword == null || keyword.isBlank() ? null : keyword.trim(),
                minCapacity, boundedPageable
        );
        List<String> propertyIds = page.getContent().stream().map(PropertyCardBaseRow::id).toList();

        if (propertyIds.isEmpty()) {
            return PageResponse.from(page.map(property -> toCard(property, Set.of(), List.of(), null, null)));
        }

        Map<String, List<String>> imageUrls = new HashMap<>();
        for (PropertyCardImageRow row : propertyImageRepository.findCardImages(propertyIds)) {
            imageUrls.computeIfAbsent(row.propertyId(), ignored -> new ArrayList<>()).add(row.url());
        }

        Map<String, Set<String>> amenities = new HashMap<>();
        for (PropertyCardAmenityRow row : propertyRepository.findCardAmenities(propertyIds)) {
            amenities.computeIfAbsent(row.propertyId(), ignored -> new LinkedHashSet<>()).add(row.name());
        }

        return PageResponse.from(page.map(property -> toCard(
                property,
                amenities.getOrDefault(property.id(), Set.of()),
                imageUrls.getOrDefault(property.id(), List.of()),
                property.ratingCount() != null && property.ratingCount() > 0 ? property.ratingAverage() : null,
                property.ratingCount()
        )));
    }

    private PropertyCardResponse toCard(
            PropertyCardBaseRow property,
            Set<String> amenities,
            List<String> imageUrls,
            Double averageRating,
            Long reviewCount
    ) {
        return new PropertyCardResponse(
                property.id(), property.slug(), property.title(), property.type(),
                property.latitude(), property.longitude(), property.city(), property.address(),
                property.capacity(), amenities, property.startingPrice(), property.endingPrice(),
                averageRating, reviewCount, imageUrls
        );
    }
}
