package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.BookingRequest;
import com.bookfarmvilla.dto.response.BookingResponse;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.entity.Booking;
import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.enums.BookingStatus;
import com.bookfarmvilla.entity.enums.PaymentStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.BookingRepository;
import com.bookfarmvilla.repository.BookingSpecifications;
import com.bookfarmvilla.repository.PropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;

    public BookingService(BookingRepository bookingRepository, PropertyRepository propertyRepository) {
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> search(
            PropertyType listingType, BookingStatus status, PaymentStatus paymentStatus, String keyword, Pageable pageable
    ) {
        Specification<Booking> spec = Specification.where(BookingSpecifications.hasListingType(listingType))
                .and(BookingSpecifications.hasStatus(status))
                .and(BookingSpecifications.hasPaymentStatus(paymentStatus))
                .and(BookingSpecifications.customerNameContains(keyword));

        Page<Booking> page = bookingRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public BookingResponse findById(String id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public BookingResponse create(BookingRequest request) {
        Booking booking = new Booking();
        booking.setId(request.id());
        applyRequest(booking, request);
        Booking saved = bookingRepository.save(booking);
        log.info("Created booking id={}, customer='{}'", saved.getId(), saved.getCustomerName());
        return toResponse(saved);
    }

    @Transactional
    public BookingResponse update(String id, BookingRequest request) {
        Booking booking = getOrThrow(id);
        applyRequest(booking, request);
        Booking saved = bookingRepository.save(booking);
        log.info("Updated booking id={}", id);
        return toResponse(saved);
    }

    @Transactional
    public void delete(String id) {
        if (!bookingRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Booking", id);
        }
        bookingRepository.deleteById(id);
        log.info("Deleted booking id={}", id);
    }

    private void applyRequest(Booking booking, BookingRequest request) {
        booking.setCustomerName(request.customerName());
        booking.setCustomerPhone(request.customerPhone());
        booking.setListingType(request.listingType());
        booking.setBookingDate(request.bookingDate());
        booking.setAmount(request.amount());
        booking.setAdvanceAmount(request.advanceAmount() != null ? request.advanceAmount() : BigDecimal.ZERO);
        booking.setCommissionPercentage(request.commissionPercentage() != null
                ? request.commissionPercentage()
                : BigDecimal.ZERO);
        booking.setCommissionAmount(request.commissionAmount() != null
                ? request.commissionAmount()
                : BigDecimal.ZERO);
        booking.setStatus(request.status() != null ? request.status() : BookingStatus.PENDING);
        booking.setPaymentStatus(request.paymentStatus() != null ? request.paymentStatus() : PaymentStatus.UNPAID);

        if (request.propertyId() != null && !request.propertyId().isBlank()) {
            Property property = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Property", request.propertyId()));
            booking.setProperty(property);
        } else {
            booking.setProperty(null);
        }
    }

    private Booking getOrThrow(String id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", id));
    }

    private BookingResponse toResponse(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getCustomerName(),
                b.getCustomerPhone(),
                b.getProperty() != null ? b.getProperty().getId() : null,
                b.getProperty() != null ? b.getProperty().getTitle() : null,
                b.getListingType(),
                b.getBookingDate(),
                b.getAmount(),
                b.getAdvanceAmount(),
                b.getCommissionPercentage(),
                b.getCommissionAmount(),
                b.getStatus(),
                b.getPaymentStatus(),
                b.getCreatedAt(),
                b.getModifiedAt()
        );
    }
}
