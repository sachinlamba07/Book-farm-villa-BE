package com.bookfarmvilla.ai;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiRateLimiter {

    private static final int MAX_REQUESTS = 12;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public boolean allow(String key) {
        Instant now = Instant.now();
        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || Duration.between(current.startedAt(), now).compareTo(WINDOW) >= 0) {
                return new Window(now, 1);
            }
            return new Window(current.startedAt(), current.count() + 1);
        });
        return window.count() <= MAX_REQUESTS;
    }

    private record Window(Instant startedAt, int count) {}
}
