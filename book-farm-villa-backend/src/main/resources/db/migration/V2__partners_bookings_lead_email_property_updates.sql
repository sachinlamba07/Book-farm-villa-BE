-- V2: Partner requests, Booking records, Lead email, Property location + price-range split.

-- 1) Leads: optional email
ALTER TABLE leads ADD COLUMN email VARCHAR(255);

-- 2) Properties: exact location + split price range into two numeric fields
ALTER TABLE properties ADD COLUMN latitude DOUBLE PRECISION;
ALTER TABLE properties ADD COLUMN longitude DOUBLE PRECISION;
ALTER TABLE properties ADD COLUMN starting_price NUMERIC(12, 2);
ALTER TABLE properties ADD COLUMN ending_price NUMERIC(12, 2);
-- price_range dropped: no reliable way to auto-split free text like "₹40,000 - ₹60,000"
-- into two numbers, so existing values are not migrated automatically. Re-enter pricing
-- for existing listings via the admin panel after this migration runs.
ALTER TABLE properties DROP COLUMN price_range;

-- 3) Partners: new table for "Partner with us" business enquiries
CREATE TABLE partners (
    id              VARCHAR(36) PRIMARY KEY,
    business_name   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    business_type   VARCHAR(100) NOT NULL,
    city            VARCHAR(100) NOT NULL,
    location        VARCHAR(500) NOT NULL,
    phone_number    VARCHAR(30) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL
);

CREATE INDEX idx_partners_status ON partners(status);

-- 4) Bookings: admin-recorded booking transactions
CREATE TABLE bookings (
    id              VARCHAR(36) PRIMARY KEY,
    customer_name   VARCHAR(255) NOT NULL,
    customer_phone  VARCHAR(30),
    property_id     VARCHAR(36) REFERENCES properties(id) ON DELETE SET NULL,
    listing_type    VARCHAR(30) NOT NULL,
    booking_date    DATE NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_status  VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL
);

CREATE INDEX idx_bookings_listing_type ON bookings(listing_type);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_payment_status ON bookings(payment_status);
CREATE INDEX idx_bookings_property_id ON bookings(property_id);
