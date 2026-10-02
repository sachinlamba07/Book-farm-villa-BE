package com.bookfarmvilla.entity;

import com.bookfarmvilla.entity.enums.BookingStatus;
import com.bookfarmvilla.entity.enums.PaymentStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An admin-recorded booking transaction. Bookings are created by the internal
 * team after confirming a booking over the phone - there is no public/self-serve
 * booking flow (see the "call a booking expert" model for this project).
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class Booking extends BaseEntity {

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    /** Optional link to the actual listing, if the admin booked a specific one. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(name = "listing_type", nullable = false)
    private PropertyType listingType;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
}
