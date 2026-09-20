package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.LoginRequest;
import com.bookfarmvilla.dto.request.RegisterRequest;
import com.bookfarmvilla.dto.request.RegisterStaffRequest;
import com.bookfarmvilla.dto.response.AuthResponse;
import com.bookfarmvilla.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Public self-registration. Always creates a GUEST-role account. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /** ADMIN-only: create a STAFF or ADMIN account for the internal team. */
    @PostMapping("/register-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> registerStaff(@Valid @RequestBody RegisterStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerStaff(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
