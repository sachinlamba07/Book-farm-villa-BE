-- Book Farm Villa - initial schema
-- All primary keys are UUID strings (varchar(36)). The application generates
-- them if not supplied by the client (see BaseEntity#onCreate).

CREATE TABLE users (
    id              VARCHAR(36) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'GUEST',
    phone           VARCHAR(30),
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL
);

CREATE TABLE amenities (
    id              VARCHAR(36) PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL
);

CREATE TABLE properties (
    id              VARCHAR(36) PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    type            VARCHAR(30) NOT NULL,
    address         VARCHAR(500),
    city            VARCHAR(100) NOT NULL,
    price_range     VARCHAR(100),
    capacity        INTEGER,
    contact_phone   VARCHAR(30) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL
);

CREATE INDEX idx_properties_city ON properties(city);
CREATE INDEX idx_properties_type ON properties(type);
CREATE INDEX idx_properties_status ON properties(status);

CREATE TABLE property_images (
    id              VARCHAR(36) PRIMARY KEY,
    property_id     VARCHAR(36) NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    url             VARCHAR(1024) NOT NULL,
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL
);

CREATE INDEX idx_property_images_property_id ON property_images(property_id);

CREATE TABLE property_amenities (
    property_id     VARCHAR(36) NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    amenity_id      VARCHAR(36) NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
    PRIMARY KEY (property_id, amenity_id)
);

CREATE TABLE vendors (
    id              VARCHAR(36) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    category        VARCHAR(30) NOT NULL,
    description     TEXT,
    city            VARCHAR(100) NOT NULL,
    price_range     VARCHAR(100),
    contact_phone   VARCHAR(30) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL
);

CREATE INDEX idx_vendors_city ON vendors(city);
CREATE INDEX idx_vendors_category ON vendors(category);
CREATE INDEX idx_vendors_status ON vendors(status);

CREATE TABLE vendor_images (
    id              VARCHAR(36) PRIMARY KEY,
    vendor_id       VARCHAR(36) NOT NULL REFERENCES vendors(id) ON DELETE CASCADE,
    url             VARCHAR(1024) NOT NULL,
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL
);

CREATE INDEX idx_vendor_images_vendor_id ON vendor_images(vendor_id);

CREATE TABLE leads (
    id              VARCHAR(36) PRIMARY KEY,
    property_id     VARCHAR(36) REFERENCES properties(id) ON DELETE SET NULL,
    vendor_id       VARCHAR(36) REFERENCES vendors(id) ON DELETE SET NULL,
    name            VARCHAR(255) NOT NULL,
    phone           VARCHAR(30) NOT NULL,
    preferred_date  DATE,
    event_type      VARCHAR(100),
    message         TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'NEW',
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL,
    CONSTRAINT chk_lead_target CHECK (
        (property_id IS NOT NULL AND vendor_id IS NULL) OR
        (property_id IS NULL AND vendor_id IS NOT NULL)
    )
);

CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_property_id ON leads(property_id);
CREATE INDEX idx_leads_vendor_id ON leads(vendor_id);

CREATE TABLE reviews (
    id              VARCHAR(36) PRIMARY KEY,
    property_id     VARCHAR(36) REFERENCES properties(id) ON DELETE CASCADE,
    vendor_id       VARCHAR(36) REFERENCES vendors(id) ON DELETE CASCADE,
    reviewer_name   VARCHAR(255) NOT NULL,
    rating          INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL,
    CONSTRAINT chk_review_target CHECK (
        (property_id IS NOT NULL AND vendor_id IS NULL) OR
        (property_id IS NULL AND vendor_id IS NOT NULL)
    )
);

CREATE INDEX idx_reviews_property_id ON reviews(property_id);
CREATE INDEX idx_reviews_vendor_id ON reviews(vendor_id);
