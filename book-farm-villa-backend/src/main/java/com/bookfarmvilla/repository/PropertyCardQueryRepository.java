package com.bookfarmvilla.repository;

import com.bookfarmvilla.dto.response.PropertyCardBaseRow;
import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Repository
public class PropertyCardQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<PropertyCardBaseRow> findCards(
            Set<PropertyType> types,
            String city,
            String keyword,
            Integer minCapacity,
            Pageable pageable
    ) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PropertyCardBaseRow> query = builder.createQuery(PropertyCardBaseRow.class);
        Root<Property> property = query.from(Property.class);
        query.select(builder.construct(
                PropertyCardBaseRow.class,
                property.get("id"),
                property.get("slug"),
                property.get("title"),
                property.get("type"),
                property.get("latitude"),
                property.get("longitude"),
                property.get("city"),
                property.get("address"),
                property.get("capacity"),
                property.get("startingPrice"),
                property.get("endingPrice")
        ));
        query.where(predicates(builder, property, types, city, keyword, minCapacity));
        query.orderBy(pageable.getSort().stream()
                .map(order -> order.isAscending()
                        ? builder.asc(property.get(order.getProperty()))
                        : builder.desc(property.get(order.getProperty())))
                .toList());

        TypedQuery<PropertyCardBaseRow> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        typedQuery.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Property> countRoot = countQuery.from(Property.class);
        countQuery.select(builder.count(countRoot));
        countQuery.where(predicates(builder, countRoot, types, city, keyword, minCapacity));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, total);
    }

    private Predicate[] predicates(
            CriteriaBuilder builder,
            Root<Property> property,
            Set<PropertyType> types,
            String city,
            String keyword,
            Integer minCapacity
    ) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(property.get("status"), ListingStatus.ACTIVE));
        if (types != null && !types.isEmpty()) {
            predicates.add(property.get("type").in(types));
        }
        if (city != null && !city.isBlank()) {
            predicates.add(builder.equal(
                    builder.lower(property.get("city")),
                    city.trim().toLowerCase(Locale.ROOT)
            ));
        }
        if (keyword != null && !keyword.isBlank()) {
            predicates.add(builder.like(
                    builder.lower(property.get("title")),
                    "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%"
            ));
        }
        if (minCapacity != null) {
            predicates.add(builder.greaterThanOrEqualTo(property.get("capacity"), minCapacity));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
