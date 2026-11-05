package com.bookfarmvilla.ai;

import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.Vendor;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.entity.enums.VendorCategory;
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
    private VendorRepository vendorRepository;
    private ReviewRepository reviewRepository;
    private AiPropertySearchService searchService;

    @BeforeEach
    void setUp() {
        propertyRepository = mock(PropertyRepository.class);
        vendorRepository = mock(VendorRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        searchService = new AiPropertySearchService(
                propertyRepository,
                vendorRepository,
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
    void filtersPropertiesInsideInclusiveBudgetRange() {
        Property belowRange = property("below-budget", "Gurugram", PropertyType.FARMHOUSE, 14999);
        Property minimum = property("minimum-budget", "Gurugram", PropertyType.FARMHOUSE, 15000);
        Property maximum = property("maximum-budget", "Gurugram", PropertyType.FARMHOUSE, 20000);
        Property aboveRange = property("above-budget", "Gurugram", PropertyType.FARMHOUSE, 20001);
        when(propertyRepository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(belowRange, minimum, maximum, aboveRange)));
        when(reviewRepository.findByPropertyId(minimum.getId())).thenReturn(List.of());
        when(reviewRepository.findByPropertyId(maximum.getId())).thenReturn(List.of());

        AiPropertySearchService.PropertySearchResult result = searchService.searchPropertiesWithAlternatives(
                new AiRequirements(
                        "Gurugram",
                        null,
                        20000,
                        15000,
                        20000,
                        "FARMHOUSE",
                        List.of(),
                        null,
                        null,
                        List.of(),
                        List.of()),
                null,
                null);

        assertEquals(List.of(), result.relaxedFilters());
        assertEquals(
                List.of(minimum.getId(), maximum.getId()),
                result.properties().stream().map(item -> item.id()).toList());
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

    @Test
    void neverReturnsAnExplicitlyExcludedPropertyTypeDuringFallback() {
        Property weddingLawn = property("noida-lawn", "Noida", PropertyType.WEDDING_LAWN, 25000);
        Property villa = property("noida-villa", "Noida", PropertyType.VILLA, 25000);
        when(propertyRepository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(weddingLawn)))
                .thenReturn(new PageImpl<>(List.of(weddingLawn, villa)));

        AiRequirements requirements = new AiRequirements(
                null,
                null,
                null,
                "WEDDING_LAWN",
                List.of(),
                null,
                null,
                List.of("WEDDING_LAWN"));

        AiPropertySearchService.PropertySearchResult result = searchService.searchPropertiesWithAlternatives(
                requirements,
                null,
                null);

        assertEquals(List.of("property type"), result.relaxedFilters());
        assertEquals(List.of(villa.getId()), result.properties().stream().map(item -> item.id()).toList());
    }

    @Test
    void excludesPreviouslyShownListingsBeforeSelectingMoreResults() {
        Property previouslyShown = property("shown-farmhouse", "Noida", PropertyType.FARMHOUSE, 18000);
        Property nextOption = property("next-farmhouse", "Noida", PropertyType.FARMHOUSE, 20000);
        when(propertyRepository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(previouslyShown, nextOption)));

        AiPropertySearchService.PropertySearchResult result = searchService.searchPropertiesWithAlternatives(
                new AiRequirements(null, null, null, "FARMHOUSE", List.of(), null, null),
                null,
                null,
                List.of(previouslyShown.getId()));

        assertEquals(List.of(nextOption.getId()), result.properties().stream().map(item -> item.id()).toList());
    }

    @Test
    void searchesEveryVendorCategoryRequestedForOneEvent() {
        Vendor photographer = vendor("event-photographer", VendorCategory.PHOTOGRAPHER);
        Vendor caterer = vendor("event-caterer", VendorCategory.CATERER);
        when(vendorRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(photographer, caterer)));
        AiRequirements requirements = new AiRequirements(
                "Noida",
                null,
                null,
                null,
                List.of(),
                "birthday",
                null,
                List.of("PHOTOGRAPHER", "CATERER"),
                List.of());

        AiPropertySearchService.VendorSearchResult result =
                searchService.searchVendorsWithAlternatives(requirements);

        assertEquals(
                List.of(VendorCategory.PHOTOGRAPHER, VendorCategory.CATERER),
                result.vendors().stream().map(item -> item.category()).toList());
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

    private Vendor vendor(String id, VendorCategory category) {
        Vendor vendor = new Vendor();
        vendor.setId(id);
        vendor.setSlug(id);
        vendor.setName(id);
        vendor.setCategory(category);
        vendor.setCity("Noida");
        vendor.setStatus(ListingStatus.ACTIVE);
        vendor.setContactPhone("9999999999");
        return vendor;
    }
}
