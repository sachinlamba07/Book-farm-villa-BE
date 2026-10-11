package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PropertyV2Response;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/properties")
public class PropertyV2Controller {

    private static final Logger log = LoggerFactory.getLogger(PropertyV2Controller.class);

    private final PropertyService propertyService;

    public PropertyV2Controller(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<PropertyV2Response>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) PropertyType type,
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("GET /api/v2/properties - city={}, type={}, status={}, minCapacity={}, keyword={}, page={}, size={}",
                city, type, status, minCapacity, keyword, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(propertyService.searchV2(city, type, status, minCapacity, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyV2Response> getById(@PathVariable String id) {
        log.info("GET /api/v2/properties/{}", id);
        return ResponseEntity.ok(propertyService.findByIdV2(id));
    }
}
