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
| GET | `/uptime` | Public | Service-process uptime in seconds; does not check the database |

After five consecutive incorrect passwords for an existing account, login returns HTTP
`423 Locked` with an account-lock message. Successful login resets the failure count.
To unlock an account manually, reset `failed_login_attempts` to zero for that user in
the database.

### Properties
| Method | Path | Auth |
|---|---|---|
| GET | `/properties?city=&type=&status=&minCapacity=&keyword=&page=&size=` | Public |
| GET | `/properties/{id}` | Public |
| GET | `/v2/properties/cards?types=FARMHOUSE,WEDDING_LAWN&city=&keyword=&minCapacity=&page=&size=` | Public — lightweight paginated property cards |
| GET | `/v2/properties?city=&type=&status=&minCapacity=&keyword=&sortBy=&sortDirection=&page=&size=` | Public — includes `originalPropertyName` |
| GET | `/v2/properties/{id}` | Public — includes `originalPropertyName` |
| POST | `/properties` | ADMIN, STAFF |
| PUT | `/properties/{id}` | ADMIN, STAFF |
| DELETE | `/properties/{id}` | ADMIN |

`type` is one of `FARMHOUSE`, `VILLA`, `WEDDING_LAWN`, `AIRBNB`. `status` is `ACTIVE` or `INACTIVE`.
Each property also carries `latitude`/`longitude` (exact map coordinates) and
`startingPrice`/`endingPrice` (numeric, replacing the old free-text `priceRange` field).
The cards endpoint accepts multiple `types` and returns only card fields: ID, slug, title,
type, coordinates, city, address, capacity, amenities, starting/ending price, average
rating, and all image URLs. It omits contact details and caps page size at 100.
Property create/update requests can include `originalPropertyName`; property v2 GET endpoints
return that value. Lead GET responses also include the referenced property's
`originalPropertyName` when the lead belongs to a property.
The v2 property list supports optional sorting by `title`, `city`, `startingPrice`,
`capacity`, `status`, or `createdAt`.

### Vendors
| Method | Path | Auth |
|---|---|---|
| GET | `/vendors?city=&category=&status=&keyword=&sortBy=&sortDirection=&page=&size=` | Public |
| GET | `/vendors/{id}` | Public |
| POST | `/vendors` | ADMIN, STAFF |
| PUT | `/vendors/{id}` | ADMIN, STAFF |
| DELETE | `/vendors/{id}` | ADMIN |

`category` is one of `CATERER`, `DECORATOR`, `PHOTOGRAPHER`, `MAKEUP_ARTIST`, `DJ`, `OTHER`.
Vendor requests accept an optional `slug`; if omitted, it is generated from the vendor
name and city. Vendor responses include the slug, and vendor detail/update/delete accepts
either the UUID or slug. The vendor list supports optional sorting by `name`, `city`,
`status`, or `createdAt`.

### Blog
| Method | Path | Auth |
|---|---|---|
| GET | `/v1/blogs?category=&keyword=&sortBy=&sortDirection=&page=&size=` | Public — published posts only |
| GET | `/v1/blogs/{slug}` | Public — published posts only |
| GET | `/v1/admin/blogs?status=&sortBy=&sortDirection=&page=&size=` | ADMIN, STAFF |
| GET | `/v1/admin/blogs/{id}` | ADMIN, STAFF |
| POST | `/v1/admin/blogs` | ADMIN, STAFF — creates a draft |
| PUT | `/v1/admin/blogs/{id}` | ADMIN, STAFF |
| POST | `/v1/admin/blogs/{id}/publish` | ADMIN, STAFF |
| POST | `/v1/admin/blogs/{id}/archive` | ADMIN, STAFF |
| PATCH | `/v1/admin/blogs/{id}/status` | ADMIN, STAFF — body: `{"status":"DRAFT|PUBLISHED|ARCHIVED"}` |
| DELETE | `/v1/admin/blogs/{id}` | ADMIN, STAFF |

Blog content is stored as sanitized HTML. Allowed formatting is limited to the
rich-text editor's supported tags, links, images, and safe text color/font-size
styles. Cover images are supplied as URLs; uploading and storing image files is
handled outside this API. Slugs are generated from titles when omitted and made
unique automatically. A published post's slug cannot be changed. The public
list returns card fields only, while the detail endpoint includes full sanitized
HTML content. Admin status values are `DRAFT`, `PUBLISHED`, and `ARCHIVED`;
changing a post to `DRAFT` unpublishes it and clears its current publication date.

### Amenities
| Method | Path | Auth |
|---|---|---|
| GET | `/amenities` | Public |
| POST | `/amenities` | ADMIN, STAFF |
| DELETE | `/amenities/{id}` | ADMIN |

### Leads ("Request a Callback")
| Method | Path | Auth |
|---|---|---|
| POST | `/leads` | Public — body needs exactly one of `propertyId` / `vendorId`. `email` is optional. |
| GET | `/leads?status=&sortBy=&sortDirection=&page=&size=` | ADMIN, STAFF |
| PATCH | `/leads/{id}/status` | ADMIN, STAFF |
The lead list supports optional sorting by `name`, `createdAt`, or `status`.

New lead notifications are sent through the Resend HTTPS API. Configure
`RESEND_API_KEY`, `EMAIL_FROM`, and `EMAIL_TO` in the backend environment.
`EMAIL_FROM` must be a sender/domain verified with Resend; `EMAIL_TO` defaults
to `info@bookfarmvilla.com`. Email delivery failures are logged and do not
discard a successfully submitted lead. SMTP is not used, so this works on
hosting platforms that block outbound SMTP.

For the supported list sorting parameters, `sortDirection` accepts `asc` or `desc`
and defaults to `asc` when `sortBy` is supplied. Null values sort last, with `id`
providing a stable tie-breaker. If `sortBy` is omitted, the existing ordering is
unchanged.

### Bookings
Admin-recorded booking transactions — there is no public booking submission flow,
these are entered by the team after confirming a booking over the phone.

| Method | Path | Auth |
|---|---|---|
| GET | `/bookings?listingType=&status=&paymentStatus=&keyword=&page=&size=` | ADMIN, STAFF |
| GET | `/bookings/{id}` | ADMIN, STAFF |
| POST | `/bookings` | ADMIN, STAFF |
| PUT | `/bookings/{id}` | ADMIN, STAFF |
| DELETE | `/bookings/{id}` | ADMIN, STAFF |

`status` is one of `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`.
`paymentStatus` is one of `UNPAID`, `PAID`, `REFUNDED`.

### Partners ("Partner with us")
| Method | Path | Auth |
|---|---|---|
| POST | `/partners` | Public |
| GET | `/partners?status=&page=&size=` | ADMIN, STAFF |
| DELETE | `/partners/{id}` | ADMIN, STAFF |

New requests always start at `status: PENDING`. There is intentionally no
PUT/PATCH for partners — only create, list, and delete, per spec. `status` can
be one of `PENDING`, `OPEN`, `ACCEPTED`, `DECLINED`, but nothing currently
transitions it after creation; add a status-update endpoint later if the
admin panel needs to accept/decline requests.

### Reviews
| Method | Path | Auth |
|---|---|---|
| GET | `/reviews?keyword=&propertyId=&vendorId=&page=&size=` | ADMIN, STAFF — paginated admin list |
| GET | `/reviews/property/{propertyId}` | Public |
| GET | `/reviews/vendor/{vendorId}` | Public |
| POST | `/reviews` | ADMIN, STAFF (moderated for now) |
| PUT | `/reviews/{id}` | ADMIN, STAFF |
| DELETE | `/reviews/{id}` | ADMIN, STAFF |

Admin search matches reviewer name, comment, property title, and vendor name.
Property and vendor options for review forms use the existing keyword-filtered
`/v2/properties` and `/vendors` list endpoints.

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
- **SMS/WhatsApp sending** — lead email notifications use Resend; add an SMS/WhatsApp provider when that channel is needed.

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


## API performance cache

Public property searches/cards/details, vendor searches/details, amenities, and published blog reads use a bounded Caffeine in-memory cache (up to 500 entries per cache, 5-minute write TTL). Listing mutations evict affected cache groups; review mutations also evict listing/detail caches because ratings are included in their responses. This is an instance-local cache: on Render Free it warms after requests and is cleared whenever the process restarts or sleeps. It does not keep a Render service awake and it is not shared between multiple backend instances. If you scale to multiple instances, move to a shared cache such as Redis and retain explicit invalidation.

The JPA property image/amenity and vendor image collections use Hibernate batch fetching to reduce lazy-loading query counts on detail/list response mapping. Cache hit rates and production response latency should be measured after deployment before tuning TTL or cache capacity.


## Performance and production configuration

- Public read-heavy results use bounded Caffeine caches. They are local to each application instance and are invalidated on property, vendor, amenity, blog, and review writes. With multiple Render instances, expect cache differences until TTL expiry; use a shared cache only when multi-instance consistency is needed.
- Cache TTLs: amenities 30 minutes; listing/card queries 60 seconds; details and public blog content 5 minutes; review lists 2 minutes. Authenticated/admin and AI responses are not cached.
- `DB_POOL_MAX_SIZE` defaults to 4 and `DB_POOL_MIN_IDLE` to 0 for a small Neon plan. Tune only after checking Neon connection limits and actual concurrency.
- `SERVER_TOMCAT_MAX_THREADS` defaults to 30 to keep memory/thread use bounded on Render Free. `JAVA_TOOL_OPTIONS` sets container-aware heap sizing.
- Flyway V9 is additive: it adds rating aggregate columns and indexes, backfills aggregate values, and maintains aggregates with a database trigger. Existing columns are not removed. Review aggregates are currently internal storage and do not alter API response JSON.
- Public GET cache headers are short-lived (30 seconds browser, 60 seconds shared cache). Do not add personalized data to these public routes without revisiting cache policy.
- AI calls use a 5-second connect timeout and 10-second read timeout. The rate limiter keys on the remote address and periodically cleans expired entries; if trusted proxy IP support is required, configure it at the server/proxy boundary rather than trusting arbitrary client headers.
- `ApiTimingFilter` logs method, path, status, and duration for API requests without logging request bodies. Actuator exposes health, metrics, and Prometheus endpoints; ensure your deployment security policy protects operational endpoints as appropriate.

## Verification notes

Run `mvn clean verify` with Java 21 and access to Maven Central before deployment. This workspace did not include Maven or Docker, so the package and Flyway migration could not be executed here. Validate V9 on a Neon staging database first, then deploy and compare p50/p95 API latency, DB query count, cache hit rate, and Render cold-start latency. A cold start includes platform wake-up time and is distinct from in-process request latency.
