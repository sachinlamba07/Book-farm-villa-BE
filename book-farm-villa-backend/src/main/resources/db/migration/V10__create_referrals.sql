-- Referral Program - referral tracking table
-- V10__create_referrals.sql

CREATE TABLE referrals (
    id              VARCHAR(36) PRIMARY KEY,
    referral_number VARCHAR(30) NOT NULL UNIQUE,
    customer_mobile VARCHAR(30) NOT NULL,
    referrer_mobile VARCHAR(30),
    event           VARCHAR(100),
    description     VARCHAR(500),
    city            VARCHAR(100),
    property_id     VARCHAR(36),
    booking_date    DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'NEW',
    booking_amount  DOUBLE PRECISION,
    reward_amount   DOUBLE PRECISION,
    created_at      TIMESTAMP NOT NULL,
    modified_at     TIMESTAMP NOT NULL
);

CREATE INDEX idx_referrals_status ON referrals(status);
CREATE INDEX idx_referrals_customer_mobile ON referrals(customer_mobile);
CREATE INDEX idx_referrals_created_at ON referrals(created_at DESC);
CREATE INDEX idx_referrals_referral_number ON referrals(referral_number);

-- Foreign key to properties (optional, nullable)
ALTER TABLE referrals ADD CONSTRAINT fk_referrals_property
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE SET NULL;