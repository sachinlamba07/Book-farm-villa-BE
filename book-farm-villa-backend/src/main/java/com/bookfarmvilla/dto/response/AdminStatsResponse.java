package com.bookfarmvilla.dto.response;

/**
 * Counts shown on the admin dashboard. Includes ACTIVE and INACTIVE listings.
 */
public record AdminStatsResponse(
        long totalListings,
        long farmhouses,
        long villas,
        long weddingLawns,
        long holidayHomes,
        long airbnbs,
        long vendors,
        long bookings,
        long enquiries,
        long partnerRequests) {
}