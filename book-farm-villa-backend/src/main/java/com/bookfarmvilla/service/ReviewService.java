package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.ReviewRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.ReviewResponse;
import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.Review;
import com.bookfarmvilla.entity.Vendor;
import com.bookfarmvilla.exception.BadRequestException;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.PropertyRepository;
import com.bookfarmvilla.repository.ReviewRepository;
import com.bookfarmvilla.repository.VendorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PropertyRepository propertyRepository;
    private final VendorRepository vendorRepository;

    public ReviewService(ReviewRepository reviewRepository, PropertyRepository propertyRepository, VendorRepository vendorRepository) {
        this.reviewRepository = reviewRepository;
        this.propertyRepository = propertyRepository;
        this.vendorRepository = vendorRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "propertyReviews", key = "#propertyId")
    public List<ReviewResponse> findByProperty(String propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw ResourceNotFoundException.of("Property", propertyId);
        }
        return reviewRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "vendorReviews", key = "#vendorId")
    public List<ReviewResponse> findByVendor(String vendorId) {
        if (!vendorRepository.existsById(vendorId)) {
            throw ResourceNotFoundException.of("Vendor", vendorId);
        }
        return reviewRepository.findByVendorId(vendorId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> search(String keyword, String propertyId, String vendorId, Pageable pageable) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedPropertyId = normalizeFilter(propertyId);
        String normalizedVendorId = normalizeFilter(vendorId);
        if (normalizedPropertyId != null && !propertyRepository.existsById(normalizedPropertyId)) {
            throw ResourceNotFoundException.of("Property", normalizedPropertyId);
        }
        if (normalizedVendorId != null && !vendorRepository.existsById(normalizedVendorId)) {
            throw ResourceNotFoundException.of("Vendor", normalizedVendorId);
        }
        if (normalizedPropertyId != null && normalizedVendorId != null) {
            throw new BadRequestException("Filter reviews by a property or vendor, not both");
        }
        Page<Review> page = reviewRepository.search(
                normalizedKeyword, normalizedPropertyId, normalizedVendorId, pageable
        );
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional
    @CacheEvict(cacheNames = {"propertySearch", "propertyV2Search", "propertyCards", "propertyDetails", "propertyV2Details", "vendorSearch", "vendorDetails", "propertyReviews", "vendorReviews"}, allEntries = true)
    public ReviewResponse create(ReviewRequest request) {
        Review review = new Review();
        review.setId(request.id());
        applyRequest(review, request);
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    @CacheEvict(cacheNames = {"propertySearch", "propertyV2Search", "propertyCards", "propertyDetails", "propertyV2Details", "vendorSearch", "vendorDetails", "propertyReviews", "vendorReviews"}, allEntries = true)
    public ReviewResponse update(String id, ReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Review", id));
        applyRequest(review, request);
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    @CacheEvict(cacheNames = {"propertySearch", "propertyV2Search", "propertyCards", "propertyDetails", "propertyV2Details", "vendorSearch", "vendorDetails", "propertyReviews", "vendorReviews"}, allEntries = true)
    public void delete(String id) {
        if (!reviewRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Review", id);
        }
        reviewRepository.deleteById(id);
    }

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getProperty() != null ? r.getProperty().getId() : null,
                r.getProperty() != null ? r.getProperty().getTitle() : null,
                r.getVendor() != null ? r.getVendor().getId() : null,
                r.getVendor() != null ? r.getVendor().getName() : null,
                r.getReviewerName(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }

    private void applyRequest(Review review, ReviewRequest request) {
        boolean hasProperty = request.propertyId() != null && !request.propertyId().isBlank();
        boolean hasVendor = request.vendorId() != null && !request.vendorId().isBlank();
        if (hasProperty == hasVendor) {
            throw new BadRequestException("A review must reference exactly one property or one vendor, not both or neither");
        }

        review.setReviewerName(request.reviewerName().trim());
        review.setRating(request.rating());
        review.setComment(request.comment());
        if (hasProperty) {
            Property property = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Property", request.propertyId()));
            review.setProperty(property);
            review.setVendor(null);
        } else {
            Vendor vendor = vendorRepository.findById(request.vendorId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Vendor", request.vendorId()));
            review.setVendor(vendor);
            review.setProperty(null);
        }
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
