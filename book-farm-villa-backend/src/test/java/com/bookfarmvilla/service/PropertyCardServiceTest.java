package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PropertyCardAmenityRow;
import com.bookfarmvilla.dto.response.PropertyCardBaseRow;
import com.bookfarmvilla.dto.response.PropertyCardImageRow;
import com.bookfarmvilla.dto.response.PropertyCardResponse;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.repository.PropertyCardQueryRepository;
import com.bookfarmvilla.repository.PropertyImageRepository;
import com.bookfarmvilla.repository.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyCardServiceTest {

    @Mock
    private PropertyCardQueryRepository propertyCardQueryRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private PropertyImageRepository propertyImageRepository;

    @InjectMocks
    private PropertyCardService propertyCardService;

    @Test
    void returnsLeanCardDataWithAllMediaAndNoContactNumber() {
        PropertyCardBaseRow property = new PropertyCardBaseRow(
                "property-id", "farmhouse-delhi", "Farmhouse", PropertyType.FARMHOUSE,
                28.5, 77.2, "Delhi", "South Delhi", 20, new BigDecimal("40000"), new BigDecimal("50000"), 4.5, 2L
        );

        when(propertyCardQueryRepository.findCards(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(property)));
        when(propertyImageRepository.findCardImages(anyList())).thenReturn(List.of(
                new PropertyCardImageRow("property-id", "https://cdn.example/front.jpg", true),
                new PropertyCardImageRow("property-id", "https://cdn.example/pool.jpg", false)
        ));
        when(propertyRepository.findCardAmenities(anyList())).thenReturn(List.of(
                new PropertyCardAmenityRow("property-id", "Swimming Pool")
        ));
        PageResponse<PropertyCardResponse> result = propertyCardService.findCards(
                Set.of(PropertyType.FARMHOUSE),
                null,
                null,
                null,
                null,
                PageRequest.of(0, 20)
        );

        PropertyCardResponse card = result.content().get(0);
        assertEquals("farmhouse-delhi", card.slug());
        assertEquals(List.of("https://cdn.example/front.jpg", "https://cdn.example/pool.jpg"), card.imageUrls());
        assertEquals(Set.of("Swimming Pool"), card.amenities());
        assertEquals(4.5, card.averageRating());
        verify(propertyImageRepository).findCardImages(List.of("property-id"));
    }

    @Test
    void acceptsIdAsTheStableSortTieBreaker() {
        when(propertyCardQueryRepository.findCards(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        propertyCardService.findCards(
                Set.of(PropertyType.FARMHOUSE),
                null,
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(
                        Sort.Order.asc("startingPrice"),
                        Sort.Order.asc("id")
                ))
        );

        verify(propertyCardQueryRepository).findCards(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void appliesMinimumAndMaximumCapacityFilters() {
        when(propertyCardQueryRepository.findCards(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        propertyCardService.findCards(
                Set.of(PropertyType.FARMHOUSE),
                null,
                null,
                10,
                20,
                PageRequest.of(0, 20)
        );

        verify(propertyCardQueryRepository).findCards(
                eq(Set.of(PropertyType.FARMHOUSE)),
                isNull(),
                isNull(),
                eq(10),
                eq(20),
                any(Pageable.class)
        );
    }
}
