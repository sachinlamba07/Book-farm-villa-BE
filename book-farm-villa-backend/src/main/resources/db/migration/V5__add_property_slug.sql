ALTER TABLE properties ADD COLUMN slug VARCHAR(255);

WITH normalized AS (
    SELECT id,
           COALESCE(
               NULLIF(trim(BOTH '-' FROM left(regexp_replace(lower(title || '-' || city), '[^a-z0-9]+', '-', 'g'), 255)), ''),
               'property'
           ) AS base_slug
    FROM properties
),
ranked AS (
    SELECT id,
           base_slug,
           row_number() OVER (PARTITION BY base_slug ORDER BY id) AS duplicate_number
    FROM normalized
)
UPDATE properties p
SET slug = CASE
               WHEN ranked.duplicate_number = 1 THEN ranked.base_slug
               ELSE ranked.base_slug || '-' || p.id
           END
FROM ranked
WHERE p.id = ranked.id;

ALTER TABLE properties ALTER COLUMN slug SET NOT NULL;
CREATE UNIQUE INDEX idx_properties_slug ON properties(slug);
