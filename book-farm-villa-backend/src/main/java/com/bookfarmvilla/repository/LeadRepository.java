package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Lead;
import com.bookfarmvilla.entity.enums.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, String> {
    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);
}
