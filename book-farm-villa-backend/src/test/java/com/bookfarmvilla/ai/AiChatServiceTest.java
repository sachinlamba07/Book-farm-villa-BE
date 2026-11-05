package com.bookfarmvilla.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiChatServiceTest {

    @Test
    void detectsRomanScriptHinglishWithoutTreatingEnglishAsHindi() {
        assertTrue(AiChatService.isHinglish("Gurugram mein farmhouse chahiye"));
        assertFalse(AiChatService.isHinglish("I need a farmhouse in Gurugram"));
    }

    @Test
    void parsesBudgetRangeWithSharedKSuffix() {
        AiChatService.BudgetRange range = AiChatService.extractBudgetRange("between 15 and 20k");

        assertEquals(15000, range.minimum());
        assertEquals(20000, range.maximum());
    }

    @Test
    void parsesBudgetRangeWhenAndIsOmitted() {
        AiChatService.BudgetRange range = AiChatService.extractBudgetRange("between 15 20k");

        assertEquals(15000, range.minimum());
        assertEquals(20000, range.maximum());
    }
}
