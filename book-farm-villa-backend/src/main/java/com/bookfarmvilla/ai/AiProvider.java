package com.bookfarmvilla.ai;

public interface AiProvider {
    String generate(String systemInstruction, String userPrompt);
}
