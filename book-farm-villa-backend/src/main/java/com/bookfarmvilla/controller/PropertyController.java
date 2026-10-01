package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.PropertyRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PropertyResponse;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.service.PropertyService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private static final Logger log = LoggerFactory.getLogger(PropertyController.class);

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<PropertyResponse>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) PropertyType type,
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("GET /api/properties - city={}, type={}, status={}, minCapacity={}, keyword={}, page={}, size={}",
                city, type, status, minCapacity, keyword, pageable.getPageNumber(), pageable.getPageSize());
        PageResponse<PropertyResponse> result = propertyService.search(city, type, status, minCapacity, keyword, pageable);
        log.info("GET /api/properties - search completed");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getById(@PathVariable String id) {
        log.info("GET /api/properties/{}", id);
        return ResponseEntity.ok(propertyService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        log.info("POST /api/properties - creating property");
        PropertyResponse created = propertyService.create(request);
        log.info("POST /api/properties - created property id={}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<PropertyResponse> update(@PathVariable String id, @Valid @RequestBody PropertyRequest request) {
        log.info("PUT /api/properties/{} - updating property", id);
        PropertyResponse updated = propertyService.update(id, request);
        log.info("PUT /api/properties/{} - updated", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("DELETE /api/properties/{}", id);
        propertyService.delete(id);
        log.info("DELETE /api/properties/{} - deleted", id);
        return ResponseEntity.noContent().build();
    }
}
