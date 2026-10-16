package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

/**
 * Composable filters for the property search endpoint. Any of these can be
 * null/omitted - Specification.where(...).and(...) simply skips null predicates.
 */
public final class PropertySpecifications {

    private PropertySpecifications() {
    }

    public static Specification<Property> hasCity(String city) {
        if (city == null || city.isBlank()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("city")), city.toLowerCase());
    }

    public static Specification<Property> hasType(PropertyType type) {
        if (type == null) return null;
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Property> hasTypes(Set<PropertyType> types) {
        if (types == null || types.isEmpty()) return null;
        return (root, query, cb) -> root.get("type").in(types);
    }

    public static Specification<Property> hasStatus(ListingStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Property> minCapacity(Integer minCapacity) {
        if (minCapacity == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity);
    }

    public static Specification<Property> titleContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }
}
