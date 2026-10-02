package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Partner;
import com.bookfarmvilla.entity.enums.PartnerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerRepository extends JpaRepository<Partner, String> {
    Page<Partner> findByStatus(PartnerStatus status, Pageable pageable);
}
