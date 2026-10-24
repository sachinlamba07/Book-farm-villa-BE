package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.Referral;
import com.bookfarmvilla.entity.enums.ReferralStatus;
import java.time.LocalDate;
import java.util.List;

public record ReferralResponse(
        String id,
        String referralNumber,
        String customerMobile,
        String referrerMobile,
        String event,
        String description,
        String city,
        String propertyId,
        LocalDate bookingDate,
        ReferralStatus status,
        Double bookingAmount,
        Double rewardAmount,
        String createdAt,
        String modifiedAt
) {
    public static ReferralResponse from(Referral referral) {
        return new ReferralResponse(
                referral.getId(),
                referral.getReferralNumber(),
                referral.getCustomerMobile(),
                referral.getReferrerMobile(),
                referral.getEvent(),
                referral.getDescription(),
                referral.getCity(),
                referral.getPropertyId(),
                referral.getBookingDate() != null ? referral.getBookingDate() : null,
                referral.getStatus(),
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