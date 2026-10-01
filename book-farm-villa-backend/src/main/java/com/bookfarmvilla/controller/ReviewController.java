package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.ReviewRequest;
import com.bookfarmvilla.dto.response.ReviewResponse;
import com.bookfarmvilla.service.ReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ReviewResponse>> findByProperty(@PathVariable String propertyId) {
        log.info("GET /api/reviews/property/{}", propertyId);
        return ResponseEntity.ok(reviewService.findByProperty(propertyId));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<ReviewResponse>> findByVendor(@PathVariable String vendorId) {
        log.info("GET /api/reviews/vendor/{}", vendorId);
        return ResponseEntity.ok(reviewService.findByVendor(vendorId));
    }

    /** Moderated: only ADMIN/STAFF can publish a review for now. Open this up later if you want public review submission + an approval flag. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
        log.info("POST /api/reviews - creating review propertyId={}, vendorId={}, rating={}",
                request.propertyId(), request.vendorId(), request.rating());
        ReviewResponse created = reviewService.create(request);
        log.info("POST /api/reviews - created review id={}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("DELETE /api/reviews/{}", id);
        reviewService.delete(id);
        log.info("DELETE /api/reviews/{} - deleted", id);
        return ResponseEntity.noContent().build();
    }
}
