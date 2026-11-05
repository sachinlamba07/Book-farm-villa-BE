package com.bookfarmvilla.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BooksyKnowledgeBaseTest {

    @Test
    void retrievesRelevantVendorAndAtHomeGuidance() {
        BooksyKnowledgeBase knowledgeBase = new BooksyKnowledgeBase();

        List<String> entries = knowledgeBase.retrieve(
                "Can I find a photographer for a birthday at home?",
                List.of());

        assertTrue(entries.stream().anyMatch(entry ->
                entry.contains("Search listed photography vendors")));
        assertTrue(entries.stream().anyMatch(entry ->
                entry.contains("Some listed vendors may provide at-home services")));
    }

    @Test
    void keepsCurrentQuestionMoreRelevantThanOlderConversation() {
        BooksyKnowledgeBase knowledgeBase = new BooksyKnowledgeBase();

        List<String> entries = knowledgeBase.retrieve(
                "What is the cancellation policy?",
                List.of(new AiChatMessage("user", "I want a farmhouse in Noida")));

        assertTrue(entries.get(0).contains("cancellation or refund policy"));
    }

    @Test
    void retrievesGuidanceForCommonHinglishTerms() {
        BooksyKnowledgeBase knowledgeBase = new BooksyKnowledgeBase();

        List<String> entries = knowledgeBase.retrieve(
                "Ghar par janamdin ke liye photographer chahiye",
                List.of());

        assertTrue(entries.stream().anyMatch(entry ->
                entry.contains("Search listed photography vendors")));
    }
}
