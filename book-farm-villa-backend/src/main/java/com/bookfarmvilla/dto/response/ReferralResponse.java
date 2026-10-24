package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.Referral;
import com.bookfarmvilla.entity.enums.ReferralStatus;
import java.time.LocalDate;
import java.util.List;

public record ReferralResponse(
        String id,
        String referralNumber,
        String referrerName,
        String referrerPhone,
        String referrerEmail,
        String refereeName,
        String refereePhone,
        String refereeEmail,
        String propertyType,
        LocalDate eventDate,
        String message,
        ReferralStatus status,
        LocalDate bookingDate,
        Double bookingAmount,
        Double rewardAmount,
        String createdAt,
        String modifiedAt
) {
    public static ReferralResponse from(Referral referral) {
        return new ReferralResponse(
                referral.getId(),
                referral.getReferralNumber(),
                referral.getReferrerName(),
                referral.getReferrerPhone(),
                referral.getReferrerEmail(),
                referral.getRefereeName(),
                referral.getRefereePhone(),
                referral.getRefereeEmail(),
                referral.getPropertyType(),
                referral.getEventDate(),
                referral.getMessage(),
                referral.getStatus(),
                referral.getBookingDate() != null ? referral.getBookingDate() : null,
                referral.getBookingAmount(),
                referral.getRewardAmount(),
                referral.getCreatedAt() != null ? referral.getCreatedAt().toString() : null,
                referral.getModifiedAt() != null ? referral.getModifiedAt().toString() : null
        );
    }

    /** Spring Data Page wrapper for referral lists. */
    public static class PageResponse {
        private final List<ReferralResponse> content;
        private final int pageNumber;
        private final int pageSize;
        private final long totalElements;
        private final int totalPages;
        private final boolean last;

        public PageResponse(List<ReferralResponse> content, int pageNumber, int pageSize, long totalElements, int totalPages, boolean last) {
            this.content = content;
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.last = last;
        }

        public List<ReferralResponse> getContent() { return content; }
        public int getPageNumber() { return pageNumber; }
        public int getPageSize() { return pageSize; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public boolean isLast() { return last; }
    }
}