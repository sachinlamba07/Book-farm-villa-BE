ALTER TABLE bookings
    ADD COLUMN customer_email VARCHAR(255),
    ADD COLUMN check_in_date DATE,
    ADD COLUMN check_out_date DATE,
    ADD COLUMN start_time TIME,
    ADD COLUMN end_time TIME,
    ADD COLUMN invoice_number VARCHAR(80),
    ADD COLUMN gstin VARCHAR(32),
    ADD COLUMN place_of_supply VARCHAR(120),
    ADD COLUMN discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN gst_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN payment_mode VARCHAR(40),
    ADD COLUMN transaction_reference VARCHAR(120),
    ADD COLUMN notes VARCHAR(2000);

UPDATE bookings
SET gstin = '06AAAAA0000A1Z5'
WHERE gstin IS NULL;

CREATE TABLE booking_line_items (
    booking_id VARCHAR(36) NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    line_order INTEGER NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity VARCHAR(80) NOT NULL,
    rate NUMERIC(12, 2) NOT NULL DEFAULT 0,
    amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    PRIMARY KEY (booking_id, line_order)
);
