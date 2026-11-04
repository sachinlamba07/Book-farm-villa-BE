CREATE TABLE booking_invoice_sequences (
    month_key VARCHAR(6) PRIMARY KEY,
    last_number BIGINT NOT NULL CHECK (last_number > 0)
);
