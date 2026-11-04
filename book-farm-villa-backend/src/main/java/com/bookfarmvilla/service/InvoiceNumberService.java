package com.bookfarmvilla.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class InvoiceNumberService {

    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_DAY_FORMAT = DateTimeFormatter.ofPattern("MMdd");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    private static final long MAX_SEQUENCE = 99_999;

    private static final String NEXT_SEQUENCE_SQL = """
            INSERT INTO booking_invoice_sequences (month_key, last_number)
            SELECT ?, COALESCE(MAX(SUBSTRING(invoice_number FROM 15 FOR 5)::BIGINT), 0) + 1
            FROM bookings
            WHERE invoice_number ~ '^BFV-[0-9]{4}-[0-9]{4}-[0-9]{5}$'
              AND SUBSTRING(invoice_number FROM 5 FOR 4)
                    || SUBSTRING(invoice_number FROM 10 FOR 2) = ?
            ON CONFLICT (month_key) DO UPDATE
            SET last_number = booking_invoice_sequences.last_number + 1
            RETURNING last_number
            """;

    private final JdbcTemplate jdbcTemplate;

    public InvoiceNumberService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String nextInvoiceNumber() {
        LocalDate today = LocalDate.now(BUSINESS_TIME_ZONE);
        String year = today.format(YEAR_FORMAT);
        String monthDay = today.format(MONTH_DAY_FORMAT);
        String month = today.format(MONTH_FORMAT);
        Long sequence = jdbcTemplate.queryForObject(
                NEXT_SEQUENCE_SQL,
                Long.class,
                month,
                month);

        if (sequence == null || sequence > MAX_SEQUENCE) {
            throw new IllegalStateException("The invoice number sequence is exhausted for " + month + ".");
        }

        return String.format(Locale.ROOT, "BFV-%s-%s-%05d", year, monthDay, sequence);
    }
}
