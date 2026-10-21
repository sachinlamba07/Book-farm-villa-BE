package com.bookfarmvilla.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiTimingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(ApiTimingFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long started = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long elapsedMs = (System.nanoTime() - started) / 1_000_000;
            String path = request.getRequestURI();
            if (path.startsWith("/api/")) {
                if (elapsedMs >= 1000 || response.getStatus() >= 500) {
                    log.warn("api_request method={} path={} status={} duration_ms={}", request.getMethod(), path, response.getStatus(), elapsedMs);
                } else {
                    log.info("api_request method={} path={} status={} duration_ms={}", request.getMethod(), path, response.getStatus(), elapsedMs);
                }
            }
        }
    }
}
