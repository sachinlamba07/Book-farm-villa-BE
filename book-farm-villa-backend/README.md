# Book Farm Villa — Backend

Spring Boot REST API for a farmhouse / villa / wedding-lawn + vendor listing platform.
Model: **browse → call a booking expert or request a callback** — there is no online
checkout/payment flow in this codebase by design.

## Tech Stack
- Java 21, Spring Boot 3.3.4
- Spring Web, Spring Data JPA, Spring Security (JWT), Bean Validation
- PostgreSQL + Flyway migrations
- Lombok, springdoc-openapi (Swagger UI)

## Quick Start (Docker)

```bash
docker compose up --build
```

This starts Postgres + the backend together. The API will be at `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Quick Start (local Maven, Postgres running separately)

1. Create a Postgres database:
   ```sql
   CREATE DATABASE bookfarmvilla;
   ```
2. Copy `.env.example` to `.env` and adjust values, or export the same variables in your shell.
3. Run:
   ```bash
   mvn spring-boot:run
   ```
4. Flyway runs automatically on startup and creates all tables.

## First Login (bootstrap admin)

On first startup, the app seeds one ADMIN account automatically (see `DataSeeder`),
using `SEED_ADMIN_EMAIL` / `SEED_ADMIN_PASSWORD` (defaults: `admin@bookfarmvilla.com` /
`ChangeMe123!`). **Log in and consider rotating this password** (there's no "change
password" endpoint yet — for now, update it directly in the database or re-seed with
new env values before first deploy).

Use this account to log in and call `/api/auth/register-staff` to create additional
STAFF/ADMIN accounts for your team.

## ID Generation

Every entity extends `BaseEntity`, which handles IDs and audit timestamps in one place:
- If a request includes an `id` (e.g. a client that generates its own UUIDs), that id is kept.
- If not, the backend generates a fresh UUID before the row is inserted.
- `created_at` is set once, on insert. `modified_at` is updated automatically on every save.

## Roles

| Role | Who | Can do |
|---|---|---|
| `GUEST` | Anyone who self-registers via `/api/auth/register` | Nothing privileged yet — reserved for future guest-facing features |
| `STAFF` | Internal team, created by an ADMIN | Create/update properties, vendors, amenities, reviews; manage leads |
| `ADMIN` | Internal team, created by an ADMIN (or the bootstrap seed) | Everything STAFF can do, plus delete listings/amenities and create new STAFF/ADMIN accounts |

There is no login requirement for browsing listings or submitting a "Request a Callback" form — those are public.

## API Overview

Base URL: `/api`

### Auth
| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/auth/register` | Public | Creates a GUEST account |
| POST | `/auth/register-staff` | ADMIN | Creates a STAFF or ADMIN account |
| POST | `/auth/login` | Public | Returns a JWT |

### Properties
| Method | Path | Auth |
|---|---|---|
| GET | `/properties?city=&type=&status=&minCapacity=&keyword=&page=&size=` | Public |
| GET | `/properties/{id}` | Public |
| POST | `/properties` | ADMIN, STAFF |
| PUT | `/properties/{id}` | ADMIN, STAFF |
| DELETE | `/properties/{id}` | ADMIN |

`type` is one of `FARMHOUSE`, `VILLA`, `WEDDING_LAWN`. `status` is `ACTIVE` or `INACTIVE`.

### Vendors
| Method | Path | Auth |
|---|---|---|
| GET | `/vendors?city=&category=&status=&keyword=&page=&size=` | Public |
| GET | `/vendors/{id}` | Public |
| POST | `/vendors` | ADMIN, STAFF |
| PUT | `/vendors/{id}` | ADMIN, STAFF |
| DELETE | `/vendors/{id}` | ADMIN |

`category` is one of `CATERER`, `DECORATOR`, `PHOTOGRAPHER`, `MAKEUP_ARTIST`, `DJ`, `OTHER`.

### Amenities
| Method | Path | Auth |
|---|---|---|
| GET | `/amenities` | Public |
| POST | `/amenities` | ADMIN, STAFF |
| DELETE | `/amenities/{id}` | ADMIN |

### Leads ("Request a Callback")
| Method | Path | Auth |
|---|---|---|
| POST | `/leads` | Public — body needs exactly one of `propertyId` / `vendorId` |
| GET | `/leads?status=&page=&size=` | ADMIN, STAFF |
| PATCH | `/leads/{id}/status` | ADMIN, STAFF |

### Reviews
| Method | Path | Auth |
|---|---|---|
| GET | `/reviews/property/{propertyId}` | Public |
| GET | `/reviews/vendor/{vendorId}` | Public |
| POST | `/reviews` | ADMIN, STAFF (moderated for now) |
| DELETE | `/reviews/{id}` | ADMIN, STAFF |

## Error Format

Every error response has the same shape:

```json
{
  "timestamp": "2026-09-18T10:15:30Z",
  "status": 404,
  "error": "Not Found",
  "message": "Property not found with id: abc-123",
  "path": "/api/properties/abc-123"
}
```

Validation errors additionally include a `fieldErrors` map of `{ field: message }`.

## Image Uploads

This codebase does **not** implement image upload/storage directly — property/vendor
requests accept a plain list of `imageUrls`. The intended flow: upload the file to
object storage (Cloudflare R2 / Backblaze B2 / S3) from the frontend or a small
pre-signed-URL endpoint you add later, then pass the resulting URL here. This keeps
the backend from having to proxy large file uploads.

## What's Deliberately Not Included

- **Payments** — bookings close over a phone call, not checkout. No payment gateway, no `Booking`/`Payment` entities.
- **Guest login flows beyond registration** — `GUEST` role exists in the schema (and is the default for `/auth/register`) but has no privileged actions yet. Extend `SecurityConfig` and add guest-specific endpoints if you build guest-facing features (e.g. saved favorites) later.
- **Real email/SMS sending** — `NotificationService` logs new leads; wire in Resend/SendGrid/Twilio there when you're ready (see the TODO comment in that class).

## Project Structure

```
src/main/java/com/bookfarmvilla/
├── config/       # Security, JWT, CORS, startup admin seeding
├── controller/   # REST endpoints
├── dto/
│   ├── request/  # Validated request bodies
│   └── response/ # Response shapes
├── entity/       # JPA entities (+ enums/)
├── exception/    # Custom exceptions + global handler
├── repository/   # Spring Data repositories + Specifications for filtering
└── service/      # Business logic
```

## Deployment Notes

- Deploy as a standalone container (Render, Railway, Fly.io) — the `Dockerfile` here builds a runnable jar image.
- Point `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` at a managed Postgres instance (Neon, Supabase, Railway).
- Set `CORS_ALLOWED_ORIGINS` to your deployed frontend's URL(s), comma-separated if more than one. To call the API from local development, also include `http://localhost:3000` or `http://localhost:5173`, whichever port the frontend uses.
- Always override `JWT_SECRET` and `SEED_ADMIN_PASSWORD` in production — the defaults are for local dev only.
