package com.bookfarmvilla.repository;

import com.bookfarmvilla.dto.response.PropertyCardImageRow;
import com.bookfarmvilla.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, String> {
    List<PropertyImage> findByPropertyId(String propertyId);

    @Query("""
            select new com.bookfarmvilla.dto.response.PropertyCardImageRow(image.property.id, image.url, image.isPrimary)
            from PropertyImage image
            where image.property.id in :propertyIds
            order by image.property.id, image.isPrimary desc, image.createdAt
            """)
    List<PropertyCardImageRow> findCardImages(@Param("propertyIds") List<String> propertyIds);
}
