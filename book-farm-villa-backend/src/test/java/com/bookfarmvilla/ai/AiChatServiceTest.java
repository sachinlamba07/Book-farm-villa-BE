package com.bookfarmvilla.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiChatServiceTest {

    @Test
    void detectsRomanScriptHinglishWithoutTreatingEnglishAsHindi() {
        assertTrue(AiChatService.isHinglish("Gurugram mein farmhouse chahiye"));
        assertFalse(AiChatService.isHinglish("I need a farmhouse in Gurugram"));
    }
}
