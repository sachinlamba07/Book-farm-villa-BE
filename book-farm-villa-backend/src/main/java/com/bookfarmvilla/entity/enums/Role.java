package com.bookfarmvilla.entity.enums;

/**
 * Role of a user in the system.
 * GUEST is the default role for anyone who self-registers (or is created implicitly).
 * STAFF and ADMIN are internal team roles used to manage listings and leads.
 */
public enum Role {
    GUEST,
    STAFF,
    ADMIN
}
