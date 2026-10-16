package com.bookfarmvilla.ai;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

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
        System.out.println("[AI] Chat request started for client " + clientKey);

        if (!rateLimiter.allow(clientKey)) {
            System.out.println("[AI] Rate limit reached for client " + clientKey);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "AI usage limit reached for a moment. Please try again shortly."));
        }

        try {
            ResponseEntity<?> response = ResponseEntity.ok(chatService.chat(request));
            System.out.println("[AI] Chat request completed in " + elapsedMillis(startedAt) + " ms");
            return response;
        } catch (AiProviderException ex) {
            System.err.println("[AI] Provider error after " + elapsedMillis(startedAt) + " ms.");
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "The AI service is temporarily unavailable. Please try again shortly."));
        } catch (Exception ex) {
            System.err.println("[AI] Chat request failed after " + elapsedMillis(startedAt) + " ms.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to process the AI chat request."));
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
