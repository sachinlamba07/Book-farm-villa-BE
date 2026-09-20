package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.Role;

public record AuthResponse(
        String token,
        String userId,
        String name,
        String email,
        Role role
) {
}
