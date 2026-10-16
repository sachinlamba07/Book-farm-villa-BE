package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, String>, JpaSpecificationExecutor<Vendor> {
    Optional<Vendor> findBySlug(String slug);
}
