package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.BookingRequest;
import com.bookfarmvilla.dto.response.BookingResponse;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.entity.enums.BookingStatus;
import com.bookfarmvilla.entity.enums.PaymentStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import com.bookfarmvilla.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * All booking endpoints are admin/staff only - bookings are recorded internally
 * after a phone call, there is no public booking submission flow.
 */
@RestController
@RequestMapping("/api/bookings")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<BookingResponse>> search(
            @RequestParam(required = false) PropertyType listingType,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("GET /api/bookings - listingType={}, status={}, paymentStatus={}, keyword={}", listingType, status, paymentStatus, keyword);
        return ResponseEntity.ok(bookingService.search(listingType, status, paymentStatus, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(bookingService.findById(id));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        log.info("POST /api/bookings - creating booking for customer='{}'", request.customerName());
        BookingResponse created = bookingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> update(@PathVariable String id, @Valid @RequestBody BookingRequest request) {
        log.info("PUT /api/bookings/{} - updating booking", id);
        return ResponseEntity.ok(bookingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("DELETE /api/bookings/{}", id);
        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
