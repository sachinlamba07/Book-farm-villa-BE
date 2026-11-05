package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Vendor;
import com.bookfarmvilla.entity.enums.VendorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, String>, JpaSpecificationExecutor<Vendor> {
    Optional<Vendor> findBySlug(String slug);

    @Query("""
            select v.category as category, count(v) as count from Vendor v
            where v.status = com.bookfarmvilla.entity.enums.ListingStatus.ACTIVE
            group by v.category
            """)
    List<ActiveVendorCategoryCount> countActiveByCategory();

    /** Distinct cities of ACTIVE vendors, for the city filter dropdown. */
    @Query("""
            select distinct v.city from Vendor v
            where v.status = com.bookfarmvilla.entity.enums.ListingStatus.ACTIVE
            order by v.city
            """)
    List<String> findActiveCities();

    interface ActiveVendorCategoryCount {
        VendorCategory getCategory();

        Long getCount();
    }
}