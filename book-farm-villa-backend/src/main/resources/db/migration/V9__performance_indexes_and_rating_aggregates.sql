-- Additive performance migration: no existing columns or data are removed.
ALTER TABLE properties ADD COLUMN IF NOT EXISTS rating_avg DOUBLE PRECISION NOT NULL DEFAULT 0;
ALTER TABLE properties ADD COLUMN IF NOT EXISTS rating_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE vendors ADD COLUMN IF NOT EXISTS rating_avg DOUBLE PRECISION NOT NULL DEFAULT 0;
ALTER TABLE vendors ADD COLUMN IF NOT EXISTS rating_count BIGINT NOT NULL DEFAULT 0;

-- Backfill current aggregate values.
UPDATE properties p SET rating_avg = COALESCE(r.avg_rating, 0), rating_count = COALESCE(r.review_count, 0)
FROM (SELECT property_id, AVG(rating)::DOUBLE PRECISION AS avg_rating, COUNT(*) AS review_count
      FROM reviews WHERE property_id IS NOT NULL GROUP BY property_id) r
WHERE p.id = r.property_id;
UPDATE properties SET rating_avg = 0, rating_count = 0
WHERE id NOT IN (SELECT property_id FROM reviews WHERE property_id IS NOT NULL);
UPDATE vendors v SET rating_avg = COALESCE(r.avg_rating, 0), rating_count = COALESCE(r.review_count, 0)
FROM (SELECT vendor_id, AVG(rating)::DOUBLE PRECISION AS avg_rating, COUNT(*) AS review_count
      FROM reviews WHERE vendor_id IS NOT NULL GROUP BY vendor_id) r
WHERE v.id = r.vendor_id;
UPDATE vendors SET rating_avg = 0, rating_count = 0
WHERE id NOT IN (SELECT vendor_id FROM reviews WHERE vendor_id IS NOT NULL);

CREATE INDEX IF NOT EXISTS idx_properties_status_city ON properties(status, city);
CREATE INDEX IF NOT EXISTS idx_properties_status_type ON properties(status, type);
CREATE INDEX IF NOT EXISTS idx_properties_status_created ON properties(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_vendors_status_city ON vendors(status, city);
CREATE INDEX IF NOT EXISTS idx_vendors_status_category ON vendors(status, category);
CREATE INDEX IF NOT EXISTS idx_reviews_property_created ON reviews(property_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reviews_vendor_created ON reviews(vendor_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_blogs_status_published ON blog_posts(status, published_at DESC);

CREATE OR REPLACE FUNCTION refresh_review_rating_aggregates() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP IN ('UPDATE', 'DELETE') THEN
        IF OLD.property_id IS NOT NULL THEN
            UPDATE properties SET rating_avg = COALESCE((SELECT AVG(rating)::DOUBLE PRECISION FROM reviews WHERE property_id = OLD.property_id), 0),
                rating_count = (SELECT COUNT(*) FROM reviews WHERE property_id = OLD.property_id) WHERE id = OLD.property_id;
        ELSIF OLD.vendor_id IS NOT NULL THEN
            UPDATE vendors SET rating_avg = COALESCE((SELECT AVG(rating)::DOUBLE PRECISION FROM reviews WHERE vendor_id = OLD.vendor_id), 0),
                rating_count = (SELECT COUNT(*) FROM reviews WHERE vendor_id = OLD.vendor_id) WHERE id = OLD.vendor_id;
        END IF;
    END IF;
    IF TG_OP IN ('INSERT', 'UPDATE') THEN
        IF NEW.property_id IS NOT NULL THEN
            UPDATE properties SET rating_avg = COALESCE((SELECT AVG(rating)::DOUBLE PRECISION FROM reviews WHERE property_id = NEW.property_id), 0),
                rating_count = (SELECT COUNT(*) FROM reviews WHERE property_id = NEW.property_id) WHERE id = NEW.property_id;
        ELSIF NEW.vendor_id IS NOT NULL THEN
            UPDATE vendors SET rating_avg = COALESCE((SELECT AVG(rating)::DOUBLE PRECISION FROM reviews WHERE vendor_id = NEW.vendor_id), 0),
                rating_count = (SELECT COUNT(*) FROM reviews WHERE vendor_id = NEW.vendor_id) WHERE id = NEW.vendor_id;
        END IF;
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS trg_refresh_review_rating_aggregates ON reviews;
CREATE TRIGGER trg_refresh_review_rating_aggregates
AFTER INSERT OR UPDATE OR DELETE ON reviews
FOR EACH ROW EXECUTE FUNCTION refresh_review_rating_aggregates();
