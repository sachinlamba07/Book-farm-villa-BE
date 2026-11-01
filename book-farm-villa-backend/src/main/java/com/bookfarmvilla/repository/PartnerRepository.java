package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Partner;
import com.bookfarmvilla.entity.enums.PartnerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartnerRepository extends JpaRepository<Partner, String> {
    Page<Partner> findByStatus(PartnerStatus status, Pageable pageable);

    @Query("""
            select p from Partner p
            where (:status is null or p.status = :status)
              and (lower(p.businessName) like :pattern
                   or lower(p.fullName) like :pattern
                   or lower(p.businessType) like :pattern
                   or lower(p.city) like :pattern
                   or lower(p.location) like :pattern
                   or lower(p.phoneNumber) like :pattern)
            """)
    Page<Partner> search(
            @Param("status") PartnerStatus status,
            @Param("pattern") String pattern,
            Pageable pageable);
}