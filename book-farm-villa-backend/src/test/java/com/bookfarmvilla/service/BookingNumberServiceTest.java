package com.bookfarmvilla.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingNumberServiceTest {

    @Test
    void generatesMonthlyBookingNumberWithFiveDigitSequence() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), any(), any()))
                .thenReturn(1L);
        BookingNumberService service = new BookingNumberService(jdbcTemplate);
        String month = LocalDate.now(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("yyyyMM"));

        assertEquals("BFV-" + month + "-00001", service.nextBookingNumber());
        verify(jdbcTemplate).queryForObject(any(String.class), eq(Long.class), eq(month), eq(month));
    }
}
