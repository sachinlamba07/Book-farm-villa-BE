package com.bookfarmvilla.service;

import com.bookfarmvilla.config.JwtUtil;
import com.bookfarmvilla.dto.request.LoginRequest;
import com.bookfarmvilla.dto.request.RegisterRequest;
import com.bookfarmvilla.dto.request.RegisterStaffRequest;
import com.bookfarmvilla.dto.response.AuthResponse;
import com.bookfarmvilla.entity.User;
import com.bookfarmvilla.entity.enums.Role;
import com.bookfarmvilla.exception.AccountLockedException;
import com.bookfarmvilla.exception.DuplicateResourceException;
import com.bookfarmvilla.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(Role.GUEST); // default type is guest, always, for public self-registration

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    /**
     * ADMIN-only: create a STAFF or ADMIN account for an internal team member.
     */
    @Transactional
    public AuthResponse registerStaff(RegisterStaffRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(request.role());

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    @Transactional(noRollbackFor = {BadCredentialsException.class, AccountLockedException.class})
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailForUpdate(request.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
            throw new AccountLockedException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            userRepository.save(user);
            if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
                throw new AccountLockedException();
            }
            throw new BadCredentialsException("Invalid email or password");
        }

        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
