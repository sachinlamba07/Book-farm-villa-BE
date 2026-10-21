package com.bookfarmvilla.repository;

import com.bookfarmvilla.dto.response.PropertyCardBaseRow;
import com.bookfarmvilla.entity.enums.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/** Projection-only listing query; avoids loading entity graphs and does not use EntityManager. */
public interface PropertyCardQueryRepository extends Repository<com.bookfarmvilla.entity.Property, String> {
    @Query(value = """
            select new com.bookfarmvilla.dto.response.PropertyCardBaseRow(
                p.id, p.slug, p.title, p.type, p.latitude, p.longitude, p.city, p.address,
                p.capacity, p.startingPrice, p.endingPrice, p.ratingAverage, p.ratingCount)
            from Property p
            where p.status = com.bookfarmvilla.entity.enums.ListingStatus.ACTIVE
              and p.type in :types
              and (:city is null or lower(p.city) = lower(:city))
              and (:keyword is null or lower(p.title) like concat('%', lower(:keyword), '%'))
              and (:minCapacity is null or p.capacity >= :minCapacity)
            """,
            countQuery = """
            select count(p) from Property p
            where p.status = com.bookfarmvilla.entity.enums.ListingStatus.ACTIVE
              and p.type in :types
              and (:city is null or lower(p.city) = lower(:city))
              and (:keyword is null or lower(p.title) like concat('%', lower(:keyword), '%'))
              and (:minCapacity is null or p.capacity >= :minCapacity)
            """)
    Page<PropertyCardBaseRow> findCards(
            @Param("types") Set<PropertyType> types,
            @Param("city") String city,
            @Param("keyword") String keyword,
            @Param("minCapacity") Integer minCapacity,
            Pageable pageable);
}
