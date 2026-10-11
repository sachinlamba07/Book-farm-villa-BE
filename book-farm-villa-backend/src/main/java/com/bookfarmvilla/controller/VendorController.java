package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.VendorRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.VendorResponse;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.VendorCategory;
import com.bookfarmvilla.service.VendorService;
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
@RequestMapping("/api/vendors")
public class VendorController {

    private static final Logger log = LoggerFactory.getLogger(VendorController.class);
    private static final Set<String> SORT_FIELDS = Set.of("name", "city", "status", "createdAt");

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<VendorResponse>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) VendorCategory category,
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Pageable sortedPageable = ListSort.apply(pageable, sortBy, sortDirection, SORT_FIELDS);
        log.info("GET /api/vendors - city={}, category={}, status={}, keyword={}, sortBy={}, sortDirection={}, page={}, size={}",
                city, category, status, keyword, sortBy, sortDirection,
                sortedPageable.getPageNumber(), sortedPageable.getPageSize());
        PageResponse<VendorResponse> result = vendorService.search(city, category, status, keyword, sortedPageable);
        log.info("GET /api/vendors - search completed");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorResponse> getById(@PathVariable String id) {
        log.info("GET /api/vendors/{}", id);
        return ResponseEntity.ok(vendorService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<VendorResponse> create(@Valid @RequestBody VendorRequest request) {
        log.info("POST /api/vendors - creating vendor");
        VendorResponse created = vendorService.create(request);
        log.info("POST /api/vendors - created vendor id={}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<VendorResponse> update(@PathVariable String id, @Valid @RequestBody VendorRequest request) {
        log.info("PUT /api/vendors/{} - updating vendor", id);
        VendorResponse updated = vendorService.update(id, request);
        log.info("PUT /api/vendors/{} - updated", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("DELETE /api/vendors/{}", id);
        vendorService.delete(id);
        log.info("DELETE /api/vendors/{} - deleted", id);
        return ResponseEntity.noContent().build();
    }
}
