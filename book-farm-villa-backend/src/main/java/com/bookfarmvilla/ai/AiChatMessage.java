package com.bookfarmvilla.ai;

import jakarta.validation.constraints.Size;

public record AiChatMessage(
        @Size(max = 20) String role,
        @Size(max = 1200) String content
) {
}
