ALTER TABLE bookings
    ADD COLUMN advance_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN commission_percentage NUMERIC(5, 2) NOT NULL DEFAULT 0,
    ADD COLUMN commission_amount NUMERIC(12, 2) NOT NULL DEFAULT 0;
