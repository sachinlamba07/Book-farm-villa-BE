package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.LeadRequest;
import com.bookfarmvilla.dto.request.LeadStatusUpdateRequest;
import com.bookfarmvilla.dto.response.LeadResponse;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.entity.enums.LeadStatus;
import com.bookfarmvilla.service.LeadService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    /** Public "Request a Callback" submission - no auth required. */
    @PostMapping
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody LeadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leadService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<PageResponse<LeadResponse>> findAll(
            @RequestParam(required = false) LeadStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(leadService.findAll(status, pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<LeadResponse> updateStatus(@PathVariable String id, @Valid @RequestBody LeadStatusUpdateRequest request) {
        return ResponseEntity.ok(leadService.updateStatus(id, request.status()));
    }
}
