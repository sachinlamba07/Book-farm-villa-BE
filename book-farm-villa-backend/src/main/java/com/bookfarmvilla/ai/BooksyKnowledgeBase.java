package com.bookfarmvilla.ai;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BooksyKnowledgeBase {
    private static final Set<String> STOP_WORDS = Set.of(
            "about", "after", "also", "and", "are", "can", "could", "does", "for", "from",
            "have", "help", "how", "into", "just", "may", "more", "need", "not", "our",
            "please", "should", "that", "the", "their", "them", "there", "this", "what",
            "when", "where", "which", "who", "with", "would", "you", "your"
    );
    private static final Map<String, String> HINGLISH_TERMS = Map.ofEntries(
            Map.entry("shaadi", "wedding"),
            Map.entry("vivah", "wedding"),
            Map.entry("janamdin", "birthday"),
            Map.entry("bday", "birthday"),
            Map.entry("ghar", "home"),
            Map.entry("gharpar", "home"),
            Map.entry("jagah", "venue"),
            Map.entry("mehmaan", "guests"),
            Map.entry("khana", "catering"),
            Map.entry("sajavat", "decoration"),
            Map.entry("tasveer", "photography"),
            Map.entry("photo", "photography"),
            Map.entry("daam", "price"),
            Map.entry("paise", "price"),
            Map.entry("kitna", "price"),
            Map.entry("nahin", "exclude"),
            Map.entry("nahi", "exclude"),
            Map.entry("cancel", "cancellation")
    );

    private final List<Entry> entries;

    public BooksyKnowledgeBase() {
        this.entries = loadEntries();
    }

    public List<String> retrieve(String message, List<AiChatMessage> history) {
        Set<String> currentTerms = tokenize(message == null ? "" : message);
        Set<String> contextTerms = tokenize(buildHistoryContext(history));
        if (currentTerms.isEmpty() && contextTerms.isEmpty()) return List.of();

        return entries.stream()
                .map(entry -> new ScoredEntry(entry, score(entry, currentTerms, contextTerms)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingInt(ScoredEntry::score).reversed())
                .limit(3)
                .map(scored -> scored.entry().question() + "\n" + scored.entry().answer())
                .toList();
    }

    private static String buildHistoryContext(List<AiChatMessage> history) {
        if (history == null) return "";
        List<String> previousUserMessages = history.stream()
                .filter(item -> item != null && "user".equalsIgnoreCase(item.role()))
                .map(AiChatMessage::content)
                .filter(content -> content != null && !content.isBlank())
                .toList();
        return previousUserMessages.stream()
                .skip(Math.max(0, previousUserMessages.size() - 3L))
                .collect(Collectors.joining(" "));
    }

    private static int score(Entry entry, Set<String> currentTerms, Set<String> contextTerms) {
        Set<String> questionTerms = tokenize(entry.question());
        Set<String> keywordTerms = tokenize(entry.keywords());
        Set<String> answerTerms = tokenize(entry.answer());
        int score = 0;
        for (String term : currentTerms) {
            if (questionTerms.contains(term)) score += 4;
            if (keywordTerms.contains(term)) score += 3;
            if (answerTerms.contains(term)) score += 2;
        }
        for (String term : contextTerms) {
            if (questionTerms.contains(term)) score++;
            if (keywordTerms.contains(term)) score++;
        }
        return score;
    }

    private static Set<String> tokenize(String text) {
        Set<String> tokens = java.util.Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(token -> token.length() >= 3 && !STOP_WORDS.contains(token))
                .collect(Collectors.toSet());
        Set<String> expanded = new HashSet<>(tokens);
        tokens.stream()
                .map(HINGLISH_TERMS::get)
                .filter(term -> term != null && !STOP_WORDS.contains(term))
                .forEach(expanded::add);
        return expanded;
    }

    private static List<Entry> loadEntries() {
        ClassPathResource resource = new ClassPathResource("ai/booksy-encyclopedia.tsv");
        List<Entry> loadedEntries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                String[] fields = line.split("\\|", 3);
                if (fields.length != 3 || fields[0].isBlank() || fields[1].isBlank() || fields[2].isBlank()) {
                    throw new IllegalStateException("Invalid Booksy encyclopedia entry at line " + lineNumber);
                }
                loadedEntries.add(new Entry(fields[0], fields[1], fields[2]));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load the Booksy encyclopedia", exception);
        }
        if (loadedEntries.isEmpty()) {
            throw new IllegalStateException("The Booksy encyclopedia contains no entries");
        }
        return List.copyOf(loadedEntries);
    }

    private record Entry(String keywords, String question, String answer) {}

    private record ScoredEntry(Entry entry, int score) {}
}
