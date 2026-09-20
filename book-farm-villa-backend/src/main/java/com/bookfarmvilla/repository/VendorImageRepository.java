package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.VendorImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorImageRepository extends JpaRepository<VendorImage, String> {
    List<VendorImage> findByVendorId(String vendorId);
}
