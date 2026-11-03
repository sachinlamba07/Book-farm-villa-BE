package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.BookingRequest;
import com.bookfarmvilla.dto.request.BookingLineItemRequest;
import com.bookfarmvilla.dto.response.BookingLineItemResponse;
import com.bookfarmvilla.dto.response.BookingResponse;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.entity.Booking;
import com.bookfarmvilla.entity.BookingLineItem;
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
import java.time.LocalDate;
import java.util.List;

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
            PropertyType listingType,
            BookingStatus status,
            PaymentStatus paymentStatus,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        Specification<Booking> spec = Specification.where(BookingSpecifications.hasListingType(listingType))
                .and(BookingSpecifications.hasStatus(status))
                .and(BookingSpecifications.hasPaymentStatus(paymentStatus))
                .and(BookingSpecifications.bookingDateFrom(fromDate))
                .and(BookingSpecifications.bookingDateTo(toDate))
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
        booking.setCustomerEmail(request.customerEmail());
        booking.setListingType(request.listingType().canonical());
        booking.setBookingDate(request.bookingDate());
        booking.setCheckInDate(request.checkInDate());
        booking.setCheckOutDate(request.checkOutDate());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setInvoiceNumber(blankToNull(request.invoiceNumber()));
        booking.setGstin(blankToNull(request.gstin()));
        booking.setPlaceOfSupply(blankToNull(request.placeOfSupply()));
        booking.getLineItems().clear();
        booking.getLineItems().addAll(toLineItems(request.lineItems()));
        booking.setAmount(request.amount());
        booking.setAdvanceAmount(request.advanceAmount() != null ? request.advanceAmount() : BigDecimal.ZERO);
        booking.setCommissionPercentage(request.commissionPercentage() != null
                ? request.commissionPercentage()
                : BigDecimal.ZERO);
        booking.setCommissionAmount(request.commissionAmount() != null
                ? request.commissionAmount()
                : BigDecimal.ZERO);
        booking.setDiscountAmount(request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO);
        booking.setGstAmount(request.gstAmount() != null ? request.gstAmount() : BigDecimal.ZERO);
        booking.setPaymentMode(blankToNull(request.paymentMode()));
        booking.setTransactionReference(blankToNull(request.transactionReference()));
        booking.setNotes(blankToNull(request.notes()));
        booking.setStatus(request.status() != null ? request.status() : BookingStatus.PENDING);
        booking.setPaymentStatus(request.paymentStatus() != null ? request.paymentStatus() : PaymentStatus.UNPAID);

        validateBookingDates(request);
        validateAmountBreakdown(request);

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
                b.getCustomerEmail(),
                b.getProperty() != null ? b.getProperty().getId() : null,
                b.getProperty() != null ? b.getProperty().getTitle() : null,
                b.getListingType(),
                b.getBookingDate(),
                b.getCheckInDate(),
                b.getCheckOutDate(),
                b.getStartTime(),
                b.getEndTime(),
                b.getInvoiceNumber(),
                b.getGstin(),
                b.getPlaceOfSupply(),
                b.getLineItems().stream()
                        .map(item -> new BookingLineItemResponse(
                                item.getDescription(),
                                item.getQuantity(),
                                item.getRate(),
                                item.getAmount()))
                        .toList(),
                b.getAmount(),
                b.getAdvanceAmount(),
                b.getCommissionPercentage(),
                b.getCommissionAmount(),
                b.getDiscountAmount(),
                b.getGstAmount(),
                b.getPaymentMode(),
                b.getTransactionReference(),
                b.getNotes(),
                b.getStatus(),
                b.getPaymentStatus(),
                b.getCreatedAt(),
                b.getModifiedAt()
        );
    }

    private List<BookingLineItem> toLineItems(List<BookingLineItemRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream().map(request -> {
            BookingLineItem item = new BookingLineItem();
            item.setDescription(request.description().trim());
            item.setQuantity(request.quantity().trim());
            item.setRate(request.rate());
            item.setAmount(request.amount());
            return item;
        }).toList();
    }

    private void validateBookingDates(BookingRequest request) {
        if (request.checkInDate() != null && request.checkOutDate() != null
                && request.checkOutDate().isBefore(request.checkInDate())) {
            throw new IllegalArgumentException("Check-out date cannot be before check-in date.");
        }
        boolean spansMultipleDays = request.checkInDate() != null
                && request.checkOutDate() != null
                && request.checkOutDate().isAfter(request.checkInDate());
        if (!spansMultipleDays && request.startTime() != null && request.endTime() != null
                && !request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
    }

    private void validateAmountBreakdown(BookingRequest request) {
        if (request.lineItems() == null || request.lineItems().isEmpty()) {
            return;
        }
        BigDecimal subtotal = request.lineItems().stream()
                .map(BookingLineItemRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        BigDecimal gst = request.gstAmount() != null ? request.gstAmount() : BigDecimal.ZERO;
        BigDecimal expectedTotal = subtotal.subtract(discount).add(gst);
        if (discount.compareTo(subtotal) > 0 || expectedTotal.signum() < 0 || request.amount() == null
                || expectedTotal.compareTo(request.amount()) != 0) {
            throw new IllegalArgumentException(
                    "Total amount must equal service subtotal minus discount plus GST.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
