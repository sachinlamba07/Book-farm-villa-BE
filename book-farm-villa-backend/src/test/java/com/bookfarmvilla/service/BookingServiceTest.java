package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.BookingLineItemRequest;
import com.bookfarmvilla.dto.request.BookingRequest;
import com.bookfarmvilla.dto.response.BookingResponse;
import com.bookfarmvilla.entity.Booking;
import com.bookfarmvilla.entity.enums.BookingStatus;
import com.bookfarmvilla.entity.enums.PaymentStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.repository.BookingRepository;
import com.bookfarmvilla.repository.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private InvoiceNumberService invoiceNumberService;

    @Mock
    private BookingNumberService bookingNumberService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void persistsAndReturnsReceiptDetails() {
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceNumberService.nextInvoiceNumber()).thenReturn("BFV-2026-1104-00001");
        when(bookingNumberService.nextBookingNumber()).thenReturn("BFV-202610-00001");

        BookingResponse response = bookingService.create(request(new BigDecimal("175.00")));

        assertEquals("BFV-2026-1104-00001", response.invoiceNumber());
        assertEquals("BFV-202610-00001", response.bookingNumber());
        assertEquals("booking-001", response.id());
        assertEquals("guest@example.com", response.customerEmail());
        assertEquals(LocalDate.of(2026, 10, 30), response.checkInDate());
        assertEquals("UPI", response.paymentMode());
        assertEquals("UTR12345", response.transactionReference());
        assertEquals("06AAAAA0000A1Z5", response.gstin());
        assertEquals(2, response.lineItems().size());
        assertEquals(new BigDecimal("175.00"), response.amount());
        assertEquals(PaymentStatus.PARTIALLY_PAID, response.paymentStatus());
    }

    @Test
    void rejectsTotalThatDoesNotMatchServicesDiscountAndGst() {
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(request(new BigDecimal("174.99"))));

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(invoiceNumberService, never()).nextInvoiceNumber();
        verify(bookingNumberService, never()).nextBookingNumber();
    }

    private BookingRequest request(BigDecimal amount) {
        return new BookingRequest(
                "booking-001",
                "Guest Name",
                "+91 98765 43210",
                "guest@example.com",
                null,
                PropertyType.FARMHOUSE,
                LocalDate.of(2026, 10, 30),
                LocalDate.of(2026, 10, 30),
                LocalDate.of(2026, 10, 31),
                LocalTime.of(14, 0),
                LocalTime.of(11, 0),
                "invoice-001",
                "06AAAAA0000A1Z5",
                "Haryana",
                List.of(
                        new BookingLineItemRequest(
                                "Venue charges", "1", new BigDecimal("100.00"), new BigDecimal("100.00")),
                        new BookingLineItemRequest(
                                "Catering", "50 Guests", new BigDecimal("1.00"), new BigDecimal("50.00"))),
                amount,
                new BigDecimal("25.00"),
                new BigDecimal("10.00"),
                new BigDecimal("12.50"),
                new BigDecimal("10.00"),
                new BigDecimal("35.00"),
                "UPI",
                "UTR12345",
                "Poolside setup requested",
                BookingStatus.CONFIRMED,
                PaymentStatus.PARTIALLY_PAID
        );
    }
}
