package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.LoginRequest;
import com.bookfarmvilla.dto.request.RegisterRequest;
import com.bookfarmvilla.dto.request.RegisterStaffRequest;
import com.bookfarmvilla.dto.response.AuthResponse;
import com.bookfarmvilla.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    private final boolean publicRegistrationEnabled;

    public AuthController(
            AuthService authService,
            @Value("${app.auth.public-registration-enabled:false}") boolean publicRegistrationEnabled) {
        this.authService = authService;
        this.publicRegistrationEnabled = publicRegistrationEnabled;
    }

    /**
     * Public self-registration (GUEST role). Disabled by default because guests
     * have
     * no features yet, and open sign-up only creates accounts that could probe the
     * API.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (!publicRegistrationEnabled) {
            throw new AccessDeniedException("Public registration is disabled");
        }
        AuthResponse response = authService.register(request);
        log.info("POST /api/auth/register - registered userId={}", response.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** ADMIN-only: create a STAFF or ADMIN account for the internal team. */
    @PostMapping("/register-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> registerStaff(@Valid @RequestBody RegisterStaffRequest request) {
        AuthResponse response = authService.registerStaff(request);
        log.info("POST /api/auth/register-staff - created userId={}, role={}", response.userId(), response.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        log.info("POST /api/auth/login - login successful userId={}, role={}", response.userId(), response.role());
        return ResponseEntity.ok(response);
    }
}