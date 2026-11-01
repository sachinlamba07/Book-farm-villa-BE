package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.LeadRequest;
import com.bookfarmvilla.dto.request.LeadStatusUpdateRequest;
import com.bookfarmvilla.dto.response.LeadResponse;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.entity.enums.LeadStatus;
import com.bookfarmvilla.service.LeadService;
import com.bookfarmvilla.util.ListSort;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private static final Logger log = LoggerFactory.getLogger(LeadController.class);
    private static final Set<String> SORT_FIELDS = Set.of("name", "createdAt", "status");

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    /** Public "Request a Callback" submission - no auth required. */
    @PostMapping
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody LeadRequest request) {
        log.info("POST /api/leads - new callback request propertyId={}, vendorId={}",
                request.propertyId(), request.vendorId());
        LeadResponse created = leadService.create(request);
        log.info("POST /api/leads - created lead id={}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<PageResponse<LeadResponse>> findAll(
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @PageableDefault(size = 20) Pageable pageable) {
        Pageable sortedPageable = ListSort.apply(pageable, sortBy, sortDirection, SORT_FIELDS);
        // The keyword itself isn't logged: it may contain a customer's name or phone
        // number.
        log.info("GET /api/leads - status={}, hasKeyword={}, sortBy={}, page={}, size={}",
                status, keyword != null && !keyword.isBlank(), sortBy,
                sortedPageable.getPageNumber(), sortedPageable.getPageSize());
        return ResponseEntity.ok(leadService.findAll(status, keyword, sortedPageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<LeadResponse> updateStatus(@PathVariable String id,
            @Valid @RequestBody LeadStatusUpdateRequest request) {
        log.info("PATCH /api/leads/{}/status - newStatus={}", id, request.status());
        LeadResponse updated = leadService.updateStatus(id, request.status());
        log.info("PATCH /api/leads/{}/status - updated", id);
        return ResponseEntity.ok(updated);
    }
}