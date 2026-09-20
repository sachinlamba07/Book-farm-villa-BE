package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, String> {
    List<PropertyImage> findByPropertyId(String propertyId);
}
