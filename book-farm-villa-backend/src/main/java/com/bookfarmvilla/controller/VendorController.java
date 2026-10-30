package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.VendorRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.VendorResponse;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.VendorCategory;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.service.VendorService;
import com.bookfarmvilla.util.ListSort;
import com.bookfarmvilla.util.RequestRoles;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
            @PageableDefault(size = 20) Pageable pageable) {
        boolean staff = RequestRoles.isStaff();
        ListingStatus effectiveStatus = staff ? status : ListingStatus.ACTIVE;
        Pageable sortedPageable = ListSort.apply(pageable, sortBy, sortDirection, SORT_FIELDS);
        log.info("GET /api/vendors - city={}, category={}, status={}, staff={}", city, category, effectiveStatus,
                staff);

        PageResponse<VendorResponse> result = vendorService.search(
                city, category, effectiveStatus, keyword, sortedPageable);
        return ResponseEntity.ok(staff ? result : result.map(VendorResponse::publicView));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorResponse> getById(@PathVariable String id) {
        log.info("GET /api/vendors/{}", id);
        VendorResponse vendor = vendorService.findById(id);
        if (RequestRoles.isStaff())
            return ResponseEntity.ok(vendor);
        if (vendor.status() != ListingStatus.ACTIVE)
            throw ResourceNotFoundException.of("Vendor", id);
        return ResponseEntity.ok(vendor.publicView());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<VendorResponse> create(@Valid @RequestBody VendorRequest request) {
        VendorResponse created = vendorService.create(request);
        log.info("POST /api/vendors - created vendor id={}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<VendorResponse> update(@PathVariable String id, @Valid @RequestBody VendorRequest request) {
        VendorResponse updated = vendorService.update(id, request);
        log.info("PUT /api/vendors/{} - updated", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        vendorService.delete(id);
        log.info("DELETE /api/vendors/{} - deleted", id);
        return ResponseEntity.noContent().build();
    }
}