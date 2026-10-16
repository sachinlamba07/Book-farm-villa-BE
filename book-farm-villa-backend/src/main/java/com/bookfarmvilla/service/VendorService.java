package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.VendorRequest;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.dto.response.VendorImageResponse;
import com.bookfarmvilla.dto.response.VendorResponse;
import com.bookfarmvilla.entity.Vendor;
import com.bookfarmvilla.entity.VendorImage;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.VendorCategory;
import com.bookfarmvilla.exception.BadRequestException;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.ReviewRepository;
import com.bookfarmvilla.repository.VendorRepository;
import com.bookfarmvilla.repository.VendorSpecifications;
import com.bookfarmvilla.util.PropertySlug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.OptionalDouble;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final ReviewRepository reviewRepository;

    public VendorService(VendorRepository vendorRepository, ReviewRepository reviewRepository) {
        this.vendorRepository = vendorRepository;
        this.reviewRepository = reviewRepository;
    }

    public PageResponse<VendorResponse> search(
            String city, VendorCategory category, ListingStatus status, String keyword, Pageable pageable
    ) {
        Specification<Vendor> spec = Specification.where(VendorSpecifications.hasCity(city))
                .and(VendorSpecifications.hasCategory(category))
                .and(VendorSpecifications.hasStatus(status))
                .and(VendorSpecifications.nameContains(keyword));

        Page<Vendor> page = vendorRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    public VendorResponse findById(String id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public VendorResponse create(VendorRequest request) {
        Vendor vendor = new Vendor();
        vendor.setId(request.id());
        applyRequest(vendor, request);
        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional
    public VendorResponse update(String id, VendorRequest request) {
        Vendor vendor = getOrThrow(id);
        applyRequest(vendor, request);
        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional
    public void delete(String id) {
        vendorRepository.delete(getOrThrow(id));
    }

    private void applyRequest(Vendor vendor, VendorRequest request) {
        vendor.setName(request.name());
        vendor.setCity(request.city());
        vendor.setSlug(resolveSlug(vendor, request.slug()));
        vendor.setCategory(request.category());
        vendor.setDescription(request.description());
        vendor.setPriceRange(request.priceRange());
        vendor.setContactPhone(request.contactPhone());
        vendor.setStatus(request.status() != null ? request.status() : ListingStatus.ACTIVE);

        if (request.imageUrls() != null) {
            vendor.getImages().clear();
            for (int i = 0; i < request.imageUrls().size(); i++) {
                VendorImage image = new VendorImage();
                image.setVendor(vendor);
                image.setUrl(request.imageUrls().get(i));
                image.setPrimary(i == 0);
                vendor.getImages().add(image);
            }
        }
    }

    private Vendor getOrThrow(String id) {
        return vendorRepository.findById(id)
                .or(() -> vendorRepository.findBySlug(id))
                .orElseThrow(() -> ResourceNotFoundException.of("Vendor", id));
    }

    private String resolveSlug(Vendor vendor, String requestedSlug) {
        boolean generated = requestedSlug == null || requestedSlug.isBlank();
        String baseSlug = generated
                ? PropertySlug.from(vendor.getName(), vendor.getCity())
                : PropertySlug.from(requestedSlug, "");
        String slug = baseSlug;
        int suffix = 2;
        while (true) {
            Vendor existing = vendorRepository.findBySlug(slug).orElse(null);
            if (existing == null || existing.getId().equals(vendor.getId())) {
                return slug;
            }
            if (!generated) {
                throw new BadRequestException("Vendor slug is already in use");
            }
            slug = PropertySlug.withSuffix(baseSlug, suffix++);
        }
    }

    private VendorResponse toResponse(Vendor v) {
        List<VendorImageResponse> images = v.getImages().stream()
                .map(img -> new VendorImageResponse(img.getId(), img.getUrl(), img.isPrimary()))
                .toList();

        OptionalDouble avgOpt = reviewRepository.findByVendorId(v.getId()).stream()
                .mapToInt(r -> r.getRating())
                .average();
        Double avgRating = avgOpt.isPresent() ? avgOpt.getAsDouble() : null;

        return new VendorResponse(
                v.getId(), v.getSlug(), v.getName(), v.getCategory(), v.getDescription(), v.getCity(),
                v.getPriceRange(), v.getContactPhone(), v.getStatus(), images, avgRating,
                v.getCreatedAt(), v.getModifiedAt()
        );
    }
}
