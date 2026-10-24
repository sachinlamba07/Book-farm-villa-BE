package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.ReferralCreateRequest;
import com.bookfarmvilla.dto.request.ReferralUpdateRequest;
import com.bookfarmvilla.dto.response.ReferralResponse;
import com.bookfarmvilla.entity.Referral;
import com.bookfarmvilla.entity.enums.ReferralStatus;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.ReferralRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRepository referralRepository;

    /**
     * Create a new referral (public endpoint - no auth required).
     * Backend generates the referral number.
     */
    @Transactional
    public ReferralResponse create(ReferralCreateRequest request) {
        // Generate referral number: BFV-REF-YYYY-XXXXXX
        String referralNumber = generateReferralNumber();

        Referral referral = new Referral();
        referral.setReferralNumber(referralNumber);
        referral.setCustomerMobile(request.customerMobile());
        referral.setReferrerMobile(request.referrerMobile());
        referral.setEvent(request.event());
        referral.setDescription(request.description());
        referral.setCity(request.city());
        referral.setPropertyId(request.propertyId());
        referral.setBookingDate(request.bookingDate());
        referral.setStatus(ReferralStatus.NEW);
        referral.setBookingAmount(null);
        referral.setRewardAmount(calculateReward(null));

        Referral saved = referralRepository.save(referral);
        return ReferralResponse.from(saved);
    }

    /**
     * List referrals with pagination, newest first, with optional status filter.
     */
    @Transactional(readOnly = true)
    public ReferralResponse.PageResponse findAll(
            String status,
            Pageable pageable
    ) {
        Page<Referral> page;
        if (status != null && !status.isBlank()) {
            ReferralStatus refStatus = ReferralStatus.valueOf(status.toUpperCase());
            page = referralRepository.findByStatus(refStatus, pageable);
        } else {
            page = referralRepository.findAll(pageable);
        }

        return new ReferralResponse.PageResponse(
                page.stream().map(ReferralResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    /**
     * Get referral details by ID.
     */
    @Transactional(readOnly = true)
    public ReferralResponse getById(String id) {
        Referral referral = referralRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Referral", id));
        return ReferralResponse.from(referral);
    }

    /**
     * Update referral: status, booking date, and booking amount.
     * Only admins/staff should call this.
     */
    @Transactional
    public ReferralResponse update(String id, ReferralUpdateRequest request) {
        Referral referral = referralRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Referral", id));

        // Update status if provided
        if (request.status() != null) {
            ReferralStatus newStatus = ReferralStatus.valueOf(request.status().toUpperCase());
            referral.setStatus(newStatus);
        }

        // Update booking date if provided
        if (request.bookingDate() != null) {
            referral.setBookingDate(request.bookingDate());
        }

        // Update booking amount if provided - recalculate reward
        if (request.bookingAmount() != null) {
            referral.setBookingAmount(request.bookingAmount());
            referral.setRewardAmount(calculateReward(request.bookingAmount()));
        }

        Referral updated = referralRepository.save(referral);
        return ReferralResponse.from(updated);
    }

    /**
     * Calculate 2% reward based on booking amount.
     * Reward = 2% of eligible booking amount.
     * If booking amount is null, unconfirmed, or cancelled, reward is 0.
     */
    private Double calculateReward(Double bookingAmount) {
        if (bookingAmount == null || bookingAmount <= 0) {
            return 0.0;
        }
        // 2% reward calculation
        double reward = bookingAmount * 0.02;
        // Round to 2 decimal places
        return Math.round(reward * 100.0) / 100.0;
    }

    /**
     * Generate referral number in format BFV-REF-YYYY-XXXXXX
     * Uses a sequence stored via native query to find max sequence per year.
     */
    private String generateReferralNumber() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        // Query the max sequence number for this year from the DB
        Integer maxSeq = referralRepository.findMaxSequenceByYear(currentYear);

        int nextSequence;
        if (maxSeq != null) {
            nextSequence = maxSeq + 1;
        } else {
            nextSequence = 1; // Start at 000001
        }

        return String.format("BFV-REF-%d-%06d", currentYear, nextSequence);
    }
}