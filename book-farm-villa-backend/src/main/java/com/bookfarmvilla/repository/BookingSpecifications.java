package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Booking;
import com.bookfarmvilla.entity.enums.BookingStatus;
import com.bookfarmvilla.entity.enums.PaymentStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import org.springframework.data.jpa.domain.Specification;

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

    public static Specification<Booking> customerNameContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return (root, query, cb) -> cb.like(cb.lower(root.get("customerName")), "%" + keyword.toLowerCase() + "%");
    }
}
