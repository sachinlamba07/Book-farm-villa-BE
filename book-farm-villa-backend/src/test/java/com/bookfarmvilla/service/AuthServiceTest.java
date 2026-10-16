package com.bookfarmvilla.service;

import com.bookfarmvilla.config.JwtUtil;
import com.bookfarmvilla.dto.request.LoginRequest;
import com.bookfarmvilla.entity.User;
import com.bookfarmvilla.exception.AccountLockedException;
import com.bookfarmvilla.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void fifthIncorrectPasswordLocksAccountAndPersistsAttemptCount() {
        User user = userWithFailedAttempts(4);
        when(userRepository.findByEmailForUpdate("guest@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "password-hash")).thenReturn(false);

        assertThrows(AccountLockedException.class,
                () -> authService.login(new LoginRequest("guest@example.com", "wrong")));

        assertEquals(5, user.getFailedLoginAttempts());
        verify(userRepository).save(user);
    }

    @Test
    void lockedAccountCannotAttemptAnotherPassword() {
        User user = userWithFailedAttempts(5);
        when(userRepository.findByEmailForUpdate("guest@example.com")).thenReturn(Optional.of(user));

        assertThrows(AccountLockedException.class,
                () -> authService.login(new LoginRequest("guest@example.com", "correct")));
    }

    @Test
    void incorrectPasswordBeforeThresholdReturnsGenericCredentialFailure() {
        User user = userWithFailedAttempts(2);
        when(userRepository.findByEmailForUpdate("guest@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "password-hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("guest@example.com", "wrong")));

        assertEquals(3, user.getFailedLoginAttempts());
        verify(userRepository).save(user);
    }

    @Test
    void successfulLoginResetsFailedAttempts() {
        User user = userWithFailedAttempts(3);
        when(userRepository.findByEmailForUpdate("guest@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", "password-hash")).thenReturn(true);
        when(jwtUtil.generateToken("user-id", "guest@example.com", "GUEST")).thenReturn("token");

        authService.login(new LoginRequest("guest@example.com", "correct"));

        assertEquals(0, user.getFailedLoginAttempts());
        verify(userRepository).save(user);
    }

    private User userWithFailedAttempts(int attempts) {
        User user = new User();
        user.setId("user-id");
        user.setEmail("guest@example.com");
        user.setPasswordHash("password-hash");
        user.setFailedLoginAttempts(attempts);
        return user;
    }
}
