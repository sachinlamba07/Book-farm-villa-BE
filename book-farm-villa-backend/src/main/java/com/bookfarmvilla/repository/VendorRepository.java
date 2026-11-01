package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, String>, JpaSpecificationExecutor<Vendor> {
    Optional<Vendor> findBySlug(String slug);

    /** Distinct cities of ACTIVE vendors, for the city filter dropdown. */
    @Query("""
            select distinct v.city from Vendor v
            where v.status = com.bookfarmvilla.entity.enums.ListingStatus.ACTIVE
            order by v.city
            """)
    List<String> findActiveCities();
}