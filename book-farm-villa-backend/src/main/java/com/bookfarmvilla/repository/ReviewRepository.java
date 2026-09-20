package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByPropertyId(String propertyId);
    List<Review> findByVendorId(String vendorId);
}
