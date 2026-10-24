package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.ReferralCreateRequest;
import com.bookfarmvilla.dto.request.ReferralUpdateRequest;
import com.bookfarmvilla.dto.response.ReferralResponse;
import com.bookfarmvilla.service.ReferralService;
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
@RequestMapping({"/api/referrals", "/api/referral"})
public class ReferralController {

    private static final Logger log = LoggerFactory.getLogger(ReferralController.class);
    private static final Set<String> SORT_FIELDS = Set.of("referralNumber", "customerMobile", "status", "createdAt");

    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    /**
     * Public referral submission - no auth required.
     * Only customer mobile is required; backend generates referral number.
     */
    @PostMapping
    public ResponseEntity<ReferralResponse> create(@Valid @RequestBody ReferralCreateRequest request) {
        log.info("POST /api/referrals - new referral customerMobile={}", request.customerMobile());
        ReferralResponse created = referralService.create(request);
        log.info("POST /api/referrals - created referral id={}, referralNumber={}", created.id(), created.referralNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Admin: List referrals with pagination, newest first.
     * Optional status filter and sorting.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ReferralResponse.PageResponse> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Pageable sortedPageable = ListSort.apply(pageable, sortBy, sortDirection, SORT_FIELDS);
        log.info("GET /api/referrals - status={}, sortBy={}, sortDirection={}, page={}, size={}",
                status, sortBy, sortDirection, sortedPageable.getPageNumber(), sortedPageable.getPageSize());
        return ResponseEntity.ok(referralService.findAll(status, sortedPageable));
    }

    /**
     * Admin: Get referral details by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ReferralResponse> getById(@PathVariable String id) {
        log.info("GET /api/referrals/{}", id);
        return ResponseEntity.ok(referralService.getById(id));
    }

    /**
     * Admin: Update referral status, booking date, and booking amount.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ReferralResponse> update(@PathVariable String id, @Valid @RequestBody ReferralUpdateRequest request) {
        log.info("PATCH /api/referrals/{} - status={}, bookingDate={}, bookingAmount={}",
                id, request.status(), request.bookingDate(), request.bookingAmount());
        ReferralResponse updated = referralService.update(id, request);
        log.info("PATCH /api/referrals/{} - updated", id);
        return ResponseEntity.ok(updated);
    }
}