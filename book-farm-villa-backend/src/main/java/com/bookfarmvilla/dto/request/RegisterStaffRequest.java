package com.bookfarmvilla.dto.request;

import com.bookfarmvilla.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ADMIN-only endpoint to create STAFF or ADMIN accounts for the internal team.
 */
public record RegisterStaffRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        String phone,

        @NotNull(message = "Role is required")
        Role role
) {
}
