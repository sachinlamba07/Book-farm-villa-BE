package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.response.ReferralResponse;
import com.bookfarmvilla.entity.Referral;
import com.bookfarmvilla.entity.enums.ReferralStatus;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.ReferralRepository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final NotificationService notificationService;

    /**
     * Public referral submission - no auth required.
     * Backend generates the referral number and sets initial status.
     * Accepts a map of form fields.
     */
    @Transactional
    public ReferralResponse create(Map<String, String> request) {
        // Generate referral number: BFV-REF-YYYY-XXXXXX
        String referralNumber = generateReferralNumber();

        Referral referral = new Referral();
        referral.setReferralNumber(referralNumber);
        referral.setReferrerName(request.get("referrerName"));
        referral.setReferrerPhone(request.get("referrerPhone"));
        referral.setReferrerEmail(request.get("referrerEmail"));
        referral.setRefereeName(request.get("refereeName"));
        referral.setRefereePhone(request.get("refereePhone"));
        referral.setRefereeEmail(request.get("refereeEmail"));
        String propertyType = request.get("propertyType");
        referral.setPropertyType(
                propertyType != null && propertyType.trim().equalsIgnoreCase("AIRBNB")
                        ? "HOLIDAY_HOME"
                        : propertyType
        );
        referral.setMessage(request.get("message"));

        // Parse event date if provided
        if (request.containsKey("eventDate") && request.get("eventDate") != null && !request.get("eventDate").isBlank()) {
            try {
                referral.setEventDate(LocalDate.parse(request.get("eventDate")));
            } catch (Exception e) {
                // Keep null if invalid date format
            }
        }

        referral.setStatus(ReferralStatus.NEW);
        referral.setBookingAmount(null);
        referral.setRewardAmount(calculateReward(null));

        Referral saved = referralRepository.save(referral);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificationService.notifyNewReferral(saved);
                }
            });
        } else {
            notificationService.notifyNewReferral(saved);
        }
        return ReferralResponse.from(saved);
    }

    /**
     * List referrals with pagination, newest first.
     * Optional status filter and keyword search across referrer/referee name and phone.
     */
    @Transactional(readOnly = true)
    public ReferralResponse.PageResponse findAll(
            String status,
            String keyword,
            Pageable pageable
    ) {
        Page<Referral> page;
        if (keyword != null && !keyword.isBlank()) {
            page = referralRepository.findByKeyword(keyword, pageable);
        } else if (status != null && !status.isBlank()) {
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
     * Accepts optional query params.
     */
    @Transactional
    public ReferralResponse update(String id, String status, String bookingDateStr, Double bookingAmount) {
        Referral referral = referralRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Referral", id));

        // Update status if provided
        if (status != null && !status.isBlank()) {
            ReferralStatus newStatus = ReferralStatus.valueOf(status.toUpperCase());
            referral.setStatus(newStatus);
        }

        // Update booking date if provided
        if (bookingDateStr != null && !bookingDateStr.isBlank()) {
            try {
                referral.setBookingDate(LocalDate.parse(bookingDateStr));
            } catch (Exception e) {
                // Ignore invalid date
            }
        }

        // Update booking amount if provided - recalculate reward
        if (bookingAmount != null && bookingAmount > 0) {
            referral.setBookingAmount(bookingAmount);
            referral.setRewardAmount(calculateReward(bookingAmount));
        } else {
            referral.setBookingAmount(null);
            referral.setRewardAmount(0.0);
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