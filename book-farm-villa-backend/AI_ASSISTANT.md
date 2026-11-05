# BookFarmVilla AI Assistant

## Provider
The AI provider is selected server-side only:

```env
AI_PROVIDER=gemini
GEMINI_API_KEY=...
GEMINI_MODEL=gemini-3.1-flash-lite
```

The frontend never receives the API key.

## Flow

Browser -> POST /api/ai/chat -> requirement extraction -> relevant verified search (when needed) -> AI response

The model does not decide which properties exist. The backend queries the existing property/vendor repositories first and sends only active, verified candidates to the model. A listing search requires a user-provided location or browser coordinates; otherwise Booksy asks for an area instead of showing arbitrary results. The response includes its intent, and the UI renders only the matching cards returned by the backend; it does not run a second, less constrained listing search. `SEARCH_BOTH` supports requests for venues and one or more service categories together.

The 60-question BookFarmVilla encyclopedia is maintained in `src/main/resources/ai/booksy-encyclopedia.tsv`. `BooksyKnowledgeBase` retrieves the most relevant entries for each turn and supplies them to the response model. Keep product facts and FAQs in that resource; listing facts, prices, capacity, and amenities must still come from current listing records.

Conversation history is used to carry requirements forward. The latest correction replaces conflicting values, explicit type exclusions remain in effect, and listing IDs already shown are excluded from "show more" results. Explicit listing detail lookups are resolved from the database and must refer to active listings.

Property search relaxes budget first, then location, and finally venue type when there is no match. Explicitly excluded venue types are never returned, even during relaxation. The response explains only relaxations performed by the backend.

## Provider cost

The current implementation uses the existing Gemini provider. A provider free tier may be available, but quotas, model access, and billing terms are controlled by Google and can change; a Gemini API key is not a guarantee of zero cost. The application does not configure OpenAI or Groq. Treat provider quota exhaustion as an expected condition.

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

When a visitor grants browser location access, the request includes latitude/longitude. The backend sorts properties with verified coordinates by distance and the UI may show distances computed from those coordinates. Listings without coordinates are never assigned an invented distance.
