ALTER TABLE bookings
    ADD COLUMN booking_number VARCHAR(20);

WITH numbered_bookings AS (
    SELECT
        id,
        'BFV-' || month_key || '-' || LPAD(
            ROW_NUMBER() OVER (PARTITION BY month_key ORDER BY created_at, id)::TEXT,
            5,
            '0'
        ) AS booking_number
    FROM (
        SELECT
            id,
            created_at,
            TO_CHAR(
                (created_at AT TIME ZONE 'UTC') AT TIME ZONE 'Asia/Kolkata',
                'YYYYMM'
            ) AS month_key
        FROM bookings
    ) existing_bookings
)
UPDATE bookings booking
SET booking_number = numbered_bookings.booking_number
FROM numbered_bookings
WHERE booking.id = numbered_bookings.id;

ALTER TABLE bookings
    ALTER COLUMN booking_number SET NOT NULL;

CREATE UNIQUE INDEX uk_bookings_booking_number
    ON bookings (booking_number);

CREATE TABLE booking_number_sequences (
    month_key VARCHAR(6) PRIMARY KEY,
    last_number BIGINT NOT NULL CHECK (last_number > 0)
);

INSERT INTO booking_number_sequences (month_key, last_number)
SELECT
    SUBSTRING(booking_number FROM 5 FOR 6),
    MAX(SUBSTRING(booking_number FROM 12 FOR 5)::BIGINT)
FROM bookings
GROUP BY SUBSTRING(booking_number FROM 5 FOR 6);
