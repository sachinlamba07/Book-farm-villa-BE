package com.bookfarmvilla.ai;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        String clientKey = resolveClientKey(httpRequest);
        if (!rateLimiter.allow(clientKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(java.util.Map.of(
                            "message", "AI usage limit reached for a moment. Please try again shortly."
                    ));
        }

        return ResponseEntity.ok(chatService.chat(request));
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
