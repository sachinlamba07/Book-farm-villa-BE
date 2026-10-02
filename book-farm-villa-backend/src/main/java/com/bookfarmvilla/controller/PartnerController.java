package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.PartnerRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.PartnerResponse;
import com.bookfarmvilla.entity.enums.PartnerStatus;
import com.bookfarmvilla.service.PartnerService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private static final Logger log = LoggerFactory.getLogger(PartnerController.class);

    private final PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    /** Public "Partner with us" submission - no auth required. */
    @PostMapping
    public ResponseEntity<PartnerResponse> create(@Valid @RequestBody PartnerRequest request) {
        log.info("POST /api/partners - new partner enquiry, business='{}'", request.businessName());
        PartnerResponse created = partnerService.create(request);
        log.info("POST /api/partners - created id={}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<PageResponse<PartnerResponse>> findAll(
            @RequestParam(required = false) PartnerStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("GET /api/partners - status={}, page={}, size={}", status, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(partnerService.findAll(status, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("DELETE /api/partners/{}", id);
        partnerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
