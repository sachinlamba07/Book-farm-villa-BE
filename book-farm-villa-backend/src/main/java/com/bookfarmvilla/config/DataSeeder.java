package com.bookfarmvilla.config;

import com.bookfarmvilla.entity.User;
import com.bookfarmvilla.entity.enums.Role;
import com.bookfarmvilla.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstraps the very first ADMIN account on startup, if none exists yet.
 * This solves the chicken-and-egg problem: /api/auth/register-staff requires
 * an existing ADMIN to call it, so the first one has to come from somewhere.
 *
 * Change the seeded password immediately after first login in a real deployment.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-admin.email}")
    private String seedAdminEmail;

    @Value("${app.seed-admin.password}")
    private String seedAdminPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        boolean anyAdminExists = userRepository.findByEmail(seedAdminEmail).isPresent();

        if (!anyAdminExists) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(seedAdminEmail);
            admin.setPasswordHash(passwordEncoder.encode(seedAdminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            log.info("Seeded initial ADMIN account -> email: {} | CHANGE THIS PASSWORD after first login.", seedAdminEmail);
        }
    }
}
