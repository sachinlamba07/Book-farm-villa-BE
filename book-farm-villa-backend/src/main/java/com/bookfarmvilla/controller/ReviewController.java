package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.ReviewRequest;
import com.bookfarmvilla.dto.request.PublicReviewRequest;
import com.bookfarmvilla.dto.response.ReviewResponse;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.entity.enums.ReviewStatus;
import com.bookfarmvilla.service.ReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<PageResponse<ReviewResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String propertyId,
            @RequestParam(required = false) String vendorId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "createdAt") Pageable pageable
    ) {
        log.info("GET /api/reviews - keyword={}, propertyId={}, vendorId={}, page={}, size={}",
                keyword, propertyId, vendorId, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(reviewService.search(keyword, propertyId, vendorId, pageable));
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

    @PostMapping("/submit")
    public ResponseEntity<ReviewResponse> submitPublic(@Valid @RequestBody PublicReviewRequest request) {
        ReviewResponse created = reviewService.submitPublic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
        log.info("POST /api/reviews - creating review propertyId={}, vendorId={}, rating={}",
                request.propertyId(), request.vendorId(), request.rating());
        ReviewResponse created = reviewService.create(request);
        log.info("POST /api/reviews - created review id={}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ReviewResponse> update(
            @PathVariable String id,
            @Valid @RequestBody ReviewRequest request
    ) {
        log.info("PUT /api/reviews/{} - updating review", id);
        ReviewResponse updated = reviewService.update(id, request);
        log.info("PUT /api/reviews/{} - updated", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("DELETE /api/reviews/{}", id);
        reviewService.delete(id);
        log.info("DELETE /api/reviews/{} - deleted", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ReviewResponse> updateStatus(
            @PathVariable String id,
            @RequestParam ReviewStatus status
    ) {
        return ResponseEntity.ok(reviewService.updateStatus(id, status));
    }
}
