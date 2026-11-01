package com.bookfarmvilla.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Helpers to check the caller's role inside controllers. */
public final class RequestRoles {

    private RequestRoles() {
    }

    /** True only for authenticated ADMIN or STAFF users (never anonymous or GUEST). */
    public static boolean isStaff() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(authority ->
                "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_STAFF".equals(authority.getAuthority()));
    }
}