package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Vendor;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.VendorCategory;
import org.springframework.data.jpa.domain.Specification;

public final class VendorSpecifications {

    private VendorSpecifications() {
    }

    public static Specification<Vendor> hasCity(String city) {
        if (city == null || city.isBlank()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("city")), city.toLowerCase());
    }

    public static Specification<Vendor> hasCategory(VendorCategory category) {
        if (category == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Vendor> hasStatus(ListingStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Vendor> nameContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
    }
}
