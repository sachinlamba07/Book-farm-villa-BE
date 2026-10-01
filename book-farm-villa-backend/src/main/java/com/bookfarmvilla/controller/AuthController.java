package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.LoginRequest;
import com.bookfarmvilla.dto.request.RegisterRequest;
import com.bookfarmvilla.dto.request.RegisterStaffRequest;
import com.bookfarmvilla.dto.response.AuthResponse;
import com.bookfarmvilla.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Public self-registration. Always creates a GUEST-role account. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - email={}", request.email());
        AuthResponse response = authService.register(request);
        log.info("POST /api/auth/register - registered userId={}, role={}", response.userId(), response.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** ADMIN-only: create a STAFF or ADMIN account for the internal team. */
    @PostMapping("/register-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> registerStaff(@Valid @RequestBody RegisterStaffRequest request) {
        log.info("POST /api/auth/register-staff - email={}, role={}", request.email(), request.role());
        AuthResponse response = authService.registerStaff(request);
        log.info("POST /api/auth/register-staff - created userId={}, role={}", response.userId(), response.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - email={}", request.email());
        AuthResponse response = authService.login(request);
        log.info("POST /api/auth/login - login successful userId={}, role={}", response.userId(), response.role());
        return ResponseEntity.ok(response);
    }
}
