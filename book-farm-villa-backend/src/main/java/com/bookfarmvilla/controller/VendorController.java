package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.VendorRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.VendorResponse;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.VendorCategory;
import com.bookfarmvilla.service.VendorService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/vendors")
public class VendorController {

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
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(vendorService.search(city, category, status, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(vendorService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<VendorResponse> create(@Valid @RequestBody VendorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<VendorResponse> update(@PathVariable String id, @Valid @RequestBody VendorRequest request) {
        return ResponseEntity.ok(vendorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        vendorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
