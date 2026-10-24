-- Referral Program - referral tracking table (updated schema)
-- V11__update_referrals_schema.sql

-- Add new columns for referrer and referee details
ALTER TABLE referrals ADD COLUMN IF NOT EXISTS referrer_name VARCHAR(100);
ALTER TABLE referrals ADD COLUMN IF NOT EXISTS referrer_phone VARCHAR(30);
ALTER TABLE referrals ADD COLUMN IF NOT EXISTS referrer_email VARCHAR(100);
ALTER TABLE referrals ADD COLUMN IF NOT EXISTS referee_name VARCHAR(100);
ALTER TABLE referrals ADD COLUMN IF NOT EXISTS referee_phone VARCHAR(30);
ALTER TABLE referrals ADD COLUMN IF NOT EXISTS referee_email VARCHAR(100);
ALTER TABLE referrals ADD COLUMN IF NOT EXISTS property_type VARCHAR(50);
ALTER TABLE referrals ADD COLUMN IF NOT EXISTS event_date DATE;
ALTER TABLE referrals ADD COLUMN IF NOT EXISTS message TEXT;

-- Remove deprecated columns
ALTER TABLE referrals DROP COLUMN IF EXISTS customer_mobile;
ALTER TABLE referrals DROP COLUMN IF EXISTS referrer_mobile;
ALTER TABLE referrals DROP COLUMN IF EXISTS event;
ALTER TABLE referrals DROP COLUMN IF EXISTS description;
ALTER TABLE referrals DROP COLUMN IF EXISTS city;

-- Add indexes for new searchable fields
CREATE INDEX IF NOT EXISTS idx_referrals_referrer_name ON referrals(referrer_name);
CREATE INDEX IF NOT EXISTS idx_referrals_referee_name ON referrals(referee_name);
CREATE INDEX IF NOT EXISTS idx_referrals_referrer_phone ON referrals(referrer_phone);
CREATE INDEX IF NOT EXISTS idx_referrals_referee_phone ON referrals(referee_phone);