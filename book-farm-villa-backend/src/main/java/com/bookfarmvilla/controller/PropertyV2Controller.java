package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PropertyCardResponse;
import com.bookfarmvilla.dto.response.PropertyV2Response;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.service.PropertyCardService;
import com.bookfarmvilla.service.PropertyService;
import com.bookfarmvilla.util.ListSort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v2/properties")
public class PropertyV2Controller {

    private static final Logger log = LoggerFactory.getLogger(PropertyV2Controller.class);
    private static final Set<String> SORT_FIELDS = Set.of("title", "city", "startingPrice", "capacity", "status", "createdAt", "ratingAverage");
    private static final Set<String> CARD_SORT_FIELDS = Set.of("title", "city", "startingPrice", "capacity", "createdAt", "ratingAverage");

    private final PropertyService propertyService;
    private final PropertyCardService propertyCardService;

    public PropertyV2Controller(PropertyService propertyService, PropertyCardService propertyCardService) {
        this.propertyService = propertyService;
        this.propertyCardService = propertyCardService;
    }

    @GetMapping("/cards")
    public ResponseEntity<PageResponse<PropertyCardResponse>> cards(
            @RequestParam(required = false) Set<PropertyType> types,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Pageable sortedPageable = ListSort.apply(pageable, sortBy, sortDirection, CARD_SORT_FIELDS);
        return ResponseEntity.ok(propertyCardService.findCards(
                types, city, keyword, minCapacity, maxCapacity, sortedPageable));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PropertyV2Response>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) PropertyType type,
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Pageable sortedPageable = ListSort.apply(pageable, sortBy, sortDirection, SORT_FIELDS);
        log.info("GET /api/v2/properties - city={}, type={}, status={}, minCapacity={}, keyword={}, sortBy={}, sortDirection={}, page={}, size={}",
                city, type, status, minCapacity, keyword, sortBy, sortDirection,
                sortedPageable.getPageNumber(), sortedPageable.getPageSize());
        return ResponseEntity.ok(propertyService.searchV2(city, type, status, minCapacity, keyword, sortedPageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyV2Response> getById(@PathVariable String id) {
        log.info("GET /api/v2/properties/{}", id);
        return ResponseEntity.ok(propertyService.findByIdV2(id));
    }
}
