package com.bookfarmvilla.ai;

import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.repository.PropertyRepository;
import com.bookfarmvilla.repository.ReviewRepository;
import com.bookfarmvilla.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiPropertySearchServiceTest {

    private PropertyRepository propertyRepository;
    private ReviewRepository reviewRepository;
    private AiPropertySearchService searchService;

    @BeforeEach
    void setUp() {
        propertyRepository = mock(PropertyRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        searchService = new AiPropertySearchService(
                propertyRepository,
                mock(VendorRepository.class),
                reviewRepository
        );
    }

    @Test
    void relaxesBudgetBeforeLocationWhenMatchingPropertiesExistInRequestedLocation() {
        Property property = property("gurugram-farmhouse", "Gurugram", PropertyType.FARMHOUSE, 18000);
        when(propertyRepository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(property)));
        when(reviewRepository.findByPropertyId(property.getId())).thenReturn(List.of());

        AiPropertySearchService.PropertySearchResult result = searchService.searchPropertiesWithAlternatives(
                new AiRequirements("Gurugram", null, 15000, "FARMHOUSE", List.of(), null, null),
                null,
                null
        );

        assertEquals(List.of("budget"), result.relaxedFilters());
        assertEquals(List.of(property.getId()), result.properties().stream().map(item -> item.id()).toList());
    }

    @Test
    void relaxesLocationAfterBudgetIfNoPropertyMatchesInRequestedArea() {
        Property property = property("noida-farmhouse", "Noida", PropertyType.FARMHOUSE, 18000);
        when(propertyRepository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()))
                .thenReturn(new PageImpl<>(List.of(property)));
        when(reviewRepository.findByPropertyId(property.getId())).thenReturn(List.of());

        AiPropertySearchService.PropertySearchResult result = searchService.searchPropertiesWithAlternatives(
                new AiRequirements("Gurugram", null, 15000, "FARMHOUSE", List.of(), null, null),
                null,
                null
        );

        assertEquals(List.of("budget", "location"), result.relaxedFilters());
        assertEquals(List.of(property.getId()), result.properties().stream().map(item -> item.id()).toList());
    }

    @Test
    void returnsVerifiedAlternativeWhenTheRequestedTypeHasNoMatch() {
        Property alternative = property("noida-villa", "Noida", PropertyType.VILLA, 25000);
        when(propertyRepository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()))
                .thenReturn(new PageImpl<>(List.of(alternative)));
        when(reviewRepository.findByPropertyId(alternative.getId())).thenReturn(List.of());

        AiPropertySearchService.PropertySearchResult result = searchService.searchPropertiesWithAlternatives(
                new AiRequirements(null, null, null, "FARMHOUSE", List.of(), null, null),
                null,
                null
        );

        assertEquals(List.of("property type"), result.relaxedFilters());
        assertFalse(result.properties().isEmpty());
        assertEquals(PropertyType.VILLA, result.properties().get(0).type());
    }

    @SuppressWarnings("unchecked")
    private Specification<Property> anySpecification() {
        return any(Specification.class);
    }

    private Property property(String id, String city, PropertyType type, int startingPrice) {
        Property property = new Property();
        property.setId(id);
        property.setSlug(id);
        property.setTitle(id);
        property.setCity(city);
        property.setType(type);
        property.setStatus(ListingStatus.ACTIVE);
        property.setContactPhone("9999999999");
        property.setStartingPrice(BigDecimal.valueOf(startingPrice));
        property.setEndingPrice(BigDecimal.valueOf(startingPrice));
        return property;
    }
}
