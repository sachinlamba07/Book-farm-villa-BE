package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PropertyCardResponse;
import com.bookfarmvilla.dto.response.PropertyV2Response;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.service.PropertyCardService;
import com.bookfarmvilla.service.PropertyService;
import com.bookfarmvilla.util.ListSort;
import com.bookfarmvilla.util.RequestRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v2/properties")
public class PropertyV2Controller {

    private static final Logger log = LoggerFactory.getLogger(PropertyV2Controller.class);
    private static final Set<String> SORT_FIELDS = Set.of("title", "city", "startingPrice", "capacity", "status",
            "createdAt", "ratingAverage");
    private static final Set<String> CARD_SORT_FIELDS = Set.of("title", "city", "startingPrice", "capacity",
            "createdAt", "ratingAverage");

    private final PropertyService propertyService;
    private final PropertyCardService propertyCardService;

    public PropertyV2Controller(PropertyService propertyService, PropertyCardService propertyCardService) {
        this.propertyService = propertyService;
        this.propertyCardService = propertyCardService;
    }

    /** Cards already exclude contact details and only include ACTIVE listings. */
    @GetMapping("/cards")
    public ResponseEntity<PageResponse<PropertyCardResponse>> cards(
            @RequestParam(required = false) Set<PropertyType> types,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable sortedPageable = ListSort.apply(pageable, sortBy, sortDirection, CARD_SORT_FIELDS);
        return ResponseEntity.ok(propertyCardService.findCards(types, city, keyword, minCapacity, sortedPageable));
    }

    /** Distinct cities of ACTIVE properties, for the public city filter. */
    @GetMapping("/cities")
    public ResponseEntity<List<String>> cities() {
        return ResponseEntity.ok(propertyService.findActiveCities());
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
            @PageableDefault(size = 20) Pageable pageable) {
        boolean staff = RequestRoles.isStaff();
        ListingStatus effectiveStatus = staff ? status : ListingStatus.ACTIVE;
        Pageable sortedPageable = ListSort.apply(pageable, sortBy, sortDirection, SORT_FIELDS);
        log.info("GET /api/v2/properties - city={}, type={}, status={}, staff={}", city, type, effectiveStatus, staff);

        PageResponse<PropertyV2Response> result = propertyService.searchV2(
                city, type, effectiveStatus, minCapacity, keyword, sortedPageable);
        return ResponseEntity.ok(staff ? result : result.map(PropertyV2Response::publicView));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyV2Response> getById(@PathVariable String id) {
        log.info("GET /api/v2/properties/{}", id);
        PropertyV2Response property = propertyService.findByIdV2(id);
        if (RequestRoles.isStaff())
            return ResponseEntity.ok(property);
        if (property.status() != ListingStatus.ACTIVE)
            throw ResourceNotFoundException.of("Property", id);
        return ResponseEntity.ok(property.publicView());
    }
}