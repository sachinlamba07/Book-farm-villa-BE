# BookFarmVilla AI Assistant

## Provider
The AI provider is selected server-side only:

```env
AI_PROVIDER=gemini
GEMINI_API_KEY=...
GEMINI_MODEL=gemini-3.5-flash-lite
```

The frontend never receives the API key.

## Flow

Browser -> POST /api/ai/chat -> requirement extraction -> PostgreSQL verified search -> AI response

The model does not decide which properties exist. The backend queries the existing property/vendor repositories first and sends only a small verified candidate set to the model.

## Free-first design

The current implementation targets Gemini's free tier. It does not configure OpenAI or Groq and does not automatically enable paid billing. Free quotas/rate limits are controlled by the provider and can change, so the application must treat quota exhaustion as an expected condition.

If Gemini is unavailable, the backend returns a deterministic fallback response instead of failing the website.

## Future providers

Add another implementation of `AiProvider`, then update `AiProviderRouter`. The frontend remains unchanged.

Examples:
- GeminiAiProvider
- GroqAiProvider
- OpenAiProvider

Selection remains a backend environment variable; there is intentionally no provider selector in the UI.

## Verified-data rule

The assistant is instructed not to invent property/vendor fields. If a field is not returned by the backend, it should say that the information is not verified.

## Near Me

The browser sends latitude/longitude after location permission. The current `Property` entity does not contain latitude/longitude, so the implementation deliberately does not invent distances. Exact nearby sorting requires verified coordinates to be added to property data later.
