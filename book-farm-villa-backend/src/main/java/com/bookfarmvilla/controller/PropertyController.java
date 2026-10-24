package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.PropertyRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PropertyResponse;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.service.PropertyService;
import com.bookfarmvilla.util.ListSort;
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

import java.util.Set;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private static final Logger log = LoggerFactory.getLogger(PropertyController.class);
    private static final Set<String> SORT_FIELDS = Set.of("title", "city", "startingPrice", "capacity", "status", "createdAt", "ratingAverage");

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
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Pageable sortedPageable = ListSort.apply(pageable, sortBy, sortDirection, SORT_FIELDS);
        log.info("GET /api/properties - city={}, type={}, status={}, minCapacity={}, keyword={}, sortBy={}, sortDirection={}, page={}, size={}",
                city, type, status, minCapacity, keyword, sortBy, sortDirection,
                sortedPageable.getPageNumber(), sortedPageable.getPageSize());
        PageResponse<PropertyResponse> result = propertyService.search(city, type, status, minCapacity, keyword, sortedPageable);
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
