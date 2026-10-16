ALTER TABLE vendors ADD COLUMN slug VARCHAR(255);

WITH normalized AS (
    SELECT id,
           COALESCE(
               NULLIF(trim(BOTH '-' FROM left(regexp_replace(lower(name || '-' || city), '[^a-z0-9]+', '-', 'g'), 255)), ''),
               'vendor'
           ) AS base_slug
    FROM vendors
),
ranked AS (
    SELECT id,
           base_slug,
           row_number() OVER (PARTITION BY base_slug ORDER BY id) AS duplicate_number
    FROM normalized
)
UPDATE vendors v
SET slug = CASE
               WHEN ranked.duplicate_number = 1 THEN ranked.base_slug
               ELSE left(ranked.base_slug, 210) || '-' || v.id
           END
FROM ranked
WHERE v.id = ranked.id;

ALTER TABLE vendors ALTER COLUMN slug SET NOT NULL;
CREATE UNIQUE INDEX idx_vendors_slug ON vendors(slug);

CREATE TABLE property_videos (
    id              VARCHAR(36) PRIMARY KEY,
    property_id     VARCHAR(36) NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    url             VARCHAR(1024) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL
);

CREATE INDEX idx_property_videos_property_id ON property_videos(property_id);

ALTER TABLE users
    ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    ADD CONSTRAINT chk_users_failed_login_attempts CHECK (failed_login_attempts >= 0);
