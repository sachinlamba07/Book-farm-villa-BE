package com.bookfarmvilla.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Stops the app from starting in production with the public default secrets.
 * With the default JWT secret anyone could forge an ADMIN token.
 * Runs during bean creation, before DataSeeder seeds the admin account.
 */
@Component
public class SecurityStartupCheck implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(SecurityStartupCheck.class);

    private static final Set<String> DEV_JWT_SECRETS = Set.of(
            "this-is-a-dev-only-secret-change-me-in-production-min-32-chars",
            "local-dev-secret-change-me-min-32-characters-long");
    private static final String DEV_SEED_PASSWORD = "ChangeMe123!";

    private final String environment;
    private final String jwtSecret;
    private final String seedAdminPassword;

    public SecurityStartupCheck(
            @Value("${app.environment:development}") String environment,
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.seed-admin.password}") String seedAdminPassword) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
        this.seedAdminPassword = seedAdminPassword;
    }

    @Override
    public void afterPropertiesSet() {
        List<String> problems = new ArrayList<>();
        if (jwtSecret == null || jwtSecret.length() < 32 || DEV_JWT_SECRETS.contains(jwtSecret)) {
            problems.add("JWT_SECRET is missing, too short or a public default");
        }
        if (DEV_SEED_PASSWORD.equals(seedAdminPassword)) {
            problems.add("SEED_ADMIN_PASSWORD is the public default");
        }
        if (problems.isEmpty())
            return;

        if ("production".equalsIgnoreCase(environment)) {
            throw new IllegalStateException("Refusing to start in production: " + String.join("; ", problems));
        }
        problems.forEach(problem -> log.warn("SECURITY (dev only, would fail in production): {}", problem));
    }
}