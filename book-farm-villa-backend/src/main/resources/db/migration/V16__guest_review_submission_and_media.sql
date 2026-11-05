ALTER TABLE reviews
    ADD COLUMN review_title VARCHAR(160),
    ADD COLUMN booking_reference VARCHAR(80),
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'APPROVED';

CREATE INDEX idx_reviews_property_status_created
    ON reviews(property_id, status, created_at DESC);
CREATE INDEX idx_reviews_vendor_status_created
    ON reviews(vendor_id, status, created_at DESC);

CREATE TABLE review_media (
    review_id VARCHAR(36) NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    media_order INTEGER NOT NULL,
    media_url VARCHAR(2048) NOT NULL,
    PRIMARY KEY (review_id, media_order)
);

CREATE OR REPLACE FUNCTION refresh_review_rating_aggregates() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP IN ('UPDATE', 'DELETE') THEN
        IF OLD.property_id IS NOT NULL THEN
            UPDATE properties
            SET rating_avg = COALESCE((
                    SELECT AVG(rating)::DOUBLE PRECISION
                    FROM reviews
                    WHERE property_id = OLD.property_id AND status = 'APPROVED'
                ), 0),
                rating_count = (
                    SELECT COUNT(*)
                    FROM reviews
                    WHERE property_id = OLD.property_id AND status = 'APPROVED'
                )
            WHERE id = OLD.property_id;
        ELSIF OLD.vendor_id IS NOT NULL THEN
            UPDATE vendors
            SET rating_avg = COALESCE((
                    SELECT AVG(rating)::DOUBLE PRECISION
                    FROM reviews
                    WHERE vendor_id = OLD.vendor_id AND status = 'APPROVED'
                ), 0),
                rating_count = (
                    SELECT COUNT(*)
                    FROM reviews
                    WHERE vendor_id = OLD.vendor_id AND status = 'APPROVED'
                )
            WHERE id = OLD.vendor_id;
        END IF;
    END IF;
    IF TG_OP IN ('INSERT', 'UPDATE') THEN
        IF NEW.property_id IS NOT NULL THEN
            UPDATE properties
            SET rating_avg = COALESCE((
                    SELECT AVG(rating)::DOUBLE PRECISION
                    FROM reviews
                    WHERE property_id = NEW.property_id AND status = 'APPROVED'
                ), 0),
                rating_count = (
                    SELECT COUNT(*)
                    FROM reviews
                    WHERE property_id = NEW.property_id AND status = 'APPROVED'
                )
            WHERE id = NEW.property_id;
        ELSIF NEW.vendor_id IS NOT NULL THEN
            UPDATE vendors
            SET rating_avg = COALESCE((
                    SELECT AVG(rating)::DOUBLE PRECISION
                    FROM reviews
                    WHERE vendor_id = NEW.vendor_id AND status = 'APPROVED'
                ), 0),
                rating_count = (
                    SELECT COUNT(*)
                    FROM reviews
                    WHERE vendor_id = NEW.vendor_id AND status = 'APPROVED'
                )
            WHERE id = NEW.vendor_id;
        END IF;
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
