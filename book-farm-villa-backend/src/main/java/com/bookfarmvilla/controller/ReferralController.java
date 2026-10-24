package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.response.ReferralBannerData;
import com.bookfarmvilla.dto.response.ReferralPolicyData;
import com.bookfarmvilla.dto.response.ReferralResponse;
import com.bookfarmvilla.dto.response.ReferralStrategySectionData;
import com.bookfarmvilla.service.ReferralService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/referrals", "/api/referral"})
public class ReferralController {

    private static final Logger log = LoggerFactory.getLogger(ReferralController.class);

    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    /**
     * Public referral submission - no auth required.
     * Only referrer and referee details are required; backend generates referral number.
     */
    @PostMapping("/submit")
    public ResponseEntity<ReferralResponse> submit(@RequestBody Map<String, String> request) {
        log.info("POST /api/referral/submit - new referral referrer={}, referee={}",
                request.get("referrerName"), request.get("refereeName"));
        ReferralResponse created = referralService.create(request);
        log.info("POST /api/referral/submit - created referral id={}, referralNumber={}",
                created.id(), created.referralNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Admin: List referrals with pagination, newest first.
     * Optional status filter and keyword search.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ReferralResponse.PageResponse> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("GET /api/referrals - status={}, keyword={}, page={}, size={}",
                status, keyword, pageable.getPageNumber(), pageable.getPageSize());
        ReferralResponse.PageResponse result = referralService.findAll(status, keyword, pageable);
        log.info("GET /api/referrals - search completed, total={}", result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    /**
     * Public: Get referral banner content for homepage display.
     * Returns null gracefully if not configured.
     */
    @GetMapping("/banner")
    public ResponseEntity<ReferralBannerData> getBanner() {
        log.info("GET /api/referral/banner");
        // Return placeholder - can be implemented with database config later
        return ResponseEntity.ok(null);
    }

    /**
     * Public: Get referral strategy section for referral page.
     * Returns null gracefully if not configured.
     */
    @GetMapping("/strategy")
    public ResponseEntity<ReferralStrategySectionData> getStrategy() {
        log.info("GET /api/referral/strategy");
        // Return placeholder - can be implemented with database config later
        return ResponseEntity.ok(null);
    }

    /**
     * Public: Get referral policy/terms for referral page.
     * Returns null gracefully if not configured.
     */
    @GetMapping("/policy")
    public ResponseEntity<ReferralPolicyData> getPolicy() {
        log.info("GET /api/referral/policy");
        // Return placeholder - can be implemented with database config later
        return ResponseEntity.ok(null);
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
    public ResponseEntity<ReferralResponse> update(@PathVariable String id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bookingDate,
            @RequestParam(required = false) Double bookingAmount) {
        log.info("PATCH /api/referrals/{} - status={}, bookingDate={}, bookingAmount={}",
                id, status, bookingDate, bookingAmount);
        ReferralResponse updated = referralService.update(id, status,
                bookingDate != null ? bookingDate : null, bookingAmount);
        log.info("PATCH /api/referrals/{} - updated", id);
        return ResponseEntity.ok(updated);
    }
}