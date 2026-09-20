package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.ReviewRequest;
import com.bookfarmvilla.dto.response.ReviewResponse;
import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.Review;
import com.bookfarmvilla.entity.Vendor;
import com.bookfarmvilla.exception.BadRequestException;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.PropertyRepository;
import com.bookfarmvilla.repository.ReviewRepository;
import com.bookfarmvilla.repository.VendorRepository;
import org.springframework.stereotype.Service;
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

    public List<ReviewResponse> findByProperty(String propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw ResourceNotFoundException.of("Property", propertyId);
        }
        return reviewRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    public List<ReviewResponse> findByVendor(String vendorId) {
        if (!vendorRepository.existsById(vendorId)) {
            throw ResourceNotFoundException.of("Vendor", vendorId);
        }
        return reviewRepository.findByVendorId(vendorId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReviewResponse create(ReviewRequest request) {
        boolean hasProperty = request.propertyId() != null && !request.propertyId().isBlank();
        boolean hasVendor = request.vendorId() != null && !request.vendorId().isBlank();

        if (hasProperty == hasVendor) {
            throw new BadRequestException("A review must reference exactly one property or one vendor, not both or neither");
        }

        Review review = new Review();
        review.setId(request.id());
        review.setReviewerName(request.reviewerName());
        review.setRating(request.rating());
        review.setComment(request.comment());

        if (hasProperty) {
            Property property = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Property", request.propertyId()));
            review.setProperty(property);
        } else {
            Vendor vendor = vendorRepository.findById(request.vendorId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Vendor", request.vendorId()));
            review.setVendor(vendor);
        }

        return toResponse(reviewRepository.save(review));
    }

    @Transactional
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
                r.getVendor() != null ? r.getVendor().getId() : null,
                r.getReviewerName(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
