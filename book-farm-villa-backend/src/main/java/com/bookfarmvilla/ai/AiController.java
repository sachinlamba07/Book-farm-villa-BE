package com.bookfarmvilla.ai;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final AiChatService chatService;
    private final AiRateLimiter rateLimiter;

    public AiController(AiChatService chatService, AiRateLimiter rateLimiter) {
        this.chatService = chatService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @Valid @RequestBody AiChatRequest request,
            HttpServletRequest httpRequest
    ) {
        long startedAt = System.nanoTime();
        String clientKey = resolveClientKey(httpRequest);
        log.debug("AI chat request started");

        if (!rateLimiter.allow(clientKey)) {
            log.warn("AI rate limit reached");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "AI usage limit reached for a moment. Please try again shortly."));
        }

        try {
            ResponseEntity<?> response = ResponseEntity.ok(chatService.chat(request));
            log.info("AI chat request completed in {} ms", elapsedMillis(startedAt));
            return response;
        } catch (AiProviderException ex) {
            log.warn("AI provider error after {} ms", elapsedMillis(startedAt));
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "The AI service is temporarily unavailable. Please try again shortly."));
        } catch (Exception ex) {
            log.error("AI chat request failed after {} ms", elapsedMillis(startedAt), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to process the AI chat request."));
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private String resolveClientKey(HttpServletRequest request) {
        // Do not trust X-Forwarded-For supplied by clients. Configure trusted proxy handling explicitly if needed.
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }
}
