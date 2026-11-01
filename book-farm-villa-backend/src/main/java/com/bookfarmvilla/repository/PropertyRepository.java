package com.bookfarmvilla.repository;

import com.bookfarmvilla.dto.response.PropertyCardAmenityRow;
import com.bookfarmvilla.dto.response.PropertyTypeCountRow;
import com.bookfarmvilla.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, String>, JpaSpecificationExecutor<Property> {
    Optional<Property> findBySlug(String slug);

    @Query("""
            select new com.bookfarmvilla.dto.response.PropertyCardAmenityRow(property.id, amenity.name)
            from Property property join property.amenities amenity
            where property.id in :propertyIds
            order by property.id, amenity.name
            """)
    List<PropertyCardAmenityRow> findCardAmenities(@Param("propertyIds") List<String> propertyIds);

    /** Distinct cities of ACTIVE properties, for the city filter dropdown. */
    @Query("""
            select distinct p.city from Property p
            where p.status = com.bookfarmvilla.entity.enums.ListingStatus.ACTIVE
            order by p.city
            """)
    List<String> findActiveCities();

    /** Number of properties per type (all statuses), for the admin dashboard. */
    @Query("""
            select new com.bookfarmvilla.dto.response.PropertyTypeCountRow(p.type, count(p))
            from Property p
            group by p.type
            """)
    List<PropertyTypeCountRow> countGroupedByType();
}