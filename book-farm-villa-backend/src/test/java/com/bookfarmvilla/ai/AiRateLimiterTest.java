package com.bookfarmvilla.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiRateLimiterTest {
    @Test
    void allowsTwelveRequestsAndRejectsTheThirteenthWithinAWindow() {
        AiRateLimiter limiter = new AiRateLimiter();
        for (int i = 0; i < 12; i++) {
            assertTrue(limiter.allow("test-client"));
        }
        assertFalse(limiter.allow("test-client"));
    }

    @Test
    void isolatesDifferentClientKeys() {
        AiRateLimiter limiter = new AiRateLimiter();
        for (int i = 0; i < 12; i++) {
            assertTrue(limiter.allow("client-a"));
        }
        assertFalse(limiter.allow("client-a"));
        assertTrue(limiter.allow("client-b"));
    }
}
