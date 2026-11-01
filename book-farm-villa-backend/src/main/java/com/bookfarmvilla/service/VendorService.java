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
import com.bookfarmvilla.repository.VendorRepository;
import com.bookfarmvilla.repository.VendorSpecifications;
import com.bookfarmvilla.util.PropertySlug;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VendorService {

    private static final int MAX_PAGE_SIZE = 100;

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "vendorSearch", key = "T(java.util.Objects).toString(#city, '') + '|' + T(java.util.Objects).toString(#category, '') + '|' + T(java.util.Objects).toString(#status, '') + '|' + T(java.util.Objects).toString(#keyword, '') + '|' + #pageable.toString()")
    public PageResponse<VendorResponse> search(
            String city, VendorCategory category, ListingStatus status, String keyword, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
                pageable.getSort());
        Specification<Vendor> spec = Specification.where(VendorSpecifications.hasCity(city))
                .and(VendorSpecifications.hasCategory(category))
                .and(VendorSpecifications.hasStatus(status))
                .and(VendorSpecifications.nameContains(keyword));

        Page<Vendor> page = vendorRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "vendorDetails", key = "#id")
    public VendorResponse findById(String id) {
        return toResponse(getOrThrow(id));
    }

    /** Distinct cities that have at least one ACTIVE vendor. */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "vendorCities", key = "'active'")
    public List<String> findActiveCities() {
        return vendorRepository.findActiveCities();
    }

    @Transactional
    @CacheEvict(cacheNames = { "vendorSearch", "vendorDetails", "vendorCities" }, allEntries = true)
    public VendorResponse create(VendorRequest request) {
        Vendor vendor = new Vendor();
        vendor.setId(request.id());
        applyRequest(vendor, request);
        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional
    @CacheEvict(cacheNames = { "vendorSearch", "vendorDetails", "vendorCities" }, allEntries = true)
    public VendorResponse update(String id, VendorRequest request) {
        Vendor vendor = getOrThrow(id);
        applyRequest(vendor, request);
        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional
    @CacheEvict(cacheNames = { "vendorSearch", "vendorDetails", "vendorCities" }, allEntries = true)
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

        Double avgRating = v.getRatingCount() != null && v.getRatingCount() > 0
                ? v.getRatingAverage()
                : null;

        return new VendorResponse(
                v.getId(), v.getSlug(), v.getName(), v.getCategory(), v.getDescription(), v.getCity(),
                v.getPriceRange(), v.getContactPhone(), v.getStatus(), images, avgRating,
                v.getCreatedAt(), v.getModifiedAt());
    }
}