package com.bookfarmvilla.entity.enums;

/**
 * Status of a "Partner with us" business enquiry (POST /api/partners).
 * New requests default to PENDING.
 */
public enum PartnerStatus {
    PENDING,
    OPEN,
    ACCEPTED,
    DECLINED
}
