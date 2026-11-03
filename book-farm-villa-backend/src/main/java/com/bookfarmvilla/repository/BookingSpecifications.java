package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Booking;
import com.bookfarmvilla.entity.enums.BookingStatus;
import com.bookfarmvilla.entity.enums.PaymentStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class BookingSpecifications {

    private BookingSpecifications() {
    }

    public static Specification<Booking> hasListingType(PropertyType type) {
        if (type == null) return null;
        PropertyType canonicalType = type.canonical();
        return (root, query, cb) -> cb.equal(root.get("listingType"), canonicalType);
    }

    public static Specification<Booking> hasStatus(BookingStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Booking> hasPaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) return null;
        return (root, query, cb) -> cb.equal(root.get("paymentStatus"), paymentStatus);
    }

    public static Specification<Booking> bookingDateFrom(LocalDate fromDate) {
        if (fromDate == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("bookingDate"), fromDate);
    }

    public static Specification<Booking> bookingDateTo(LocalDate toDate) {
        if (toDate == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("bookingDate"), toDate);
    }

    public static Specification<Booking> customerNameContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("customerName")), pattern),
                cb.like(cb.lower(root.get("id")), pattern),
                cb.like(cb.lower(root.get("customerPhone")), pattern),
                cb.like(cb.lower(root.get("invoiceNumber")), pattern));
    }
}
