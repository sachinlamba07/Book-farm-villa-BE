package com.bookfarmvilla.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class BookingNumberService {

    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    private static final long MAX_SEQUENCE = 99_999;

    private static final String NEXT_SEQUENCE_SQL = """
            INSERT INTO booking_number_sequences (month_key, last_number)
            SELECT ?, COALESCE(MAX(SUBSTRING(booking_number FROM 12 FOR 5)::BIGINT), 0) + 1
            FROM bookings
            WHERE booking_number ~ '^BFV-[0-9]{6}-[0-9]{5}$'
              AND SUBSTRING(booking_number FROM 5 FOR 6) = ?
            ON CONFLICT (month_key) DO UPDATE
            SET last_number = booking_number_sequences.last_number + 1
            RETURNING last_number
            """;

    private final JdbcTemplate jdbcTemplate;

    public BookingNumberService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String nextBookingNumber() {
        String month = LocalDate.now(BUSINESS_TIME_ZONE).format(MONTH_FORMAT);
        Long sequence = jdbcTemplate.queryForObject(
                NEXT_SEQUENCE_SQL,
                Long.class,
                month,
                month);

        if (sequence == null || sequence > MAX_SEQUENCE) {
            throw new IllegalStateException("The booking number sequence is exhausted for " + month + ".");
        }

        return String.format(Locale.ROOT, "BFV-%s-%05d", month, sequence);
    }
}
