package com.bookfarmvilla.entity.enums;

/**
 * Status values for a referral record.
 * Order matters: NEW → CONTACTED → CONVERTED → CANCELLED.
 */
public enum ReferralStatus {
    NEW,
    CONTACTED,
    CONVERTED,
    CANCELLED
}