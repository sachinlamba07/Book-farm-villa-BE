package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Property;
import com.bookfarmvilla.dto.response.PropertyCardAmenityRow;
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
}
