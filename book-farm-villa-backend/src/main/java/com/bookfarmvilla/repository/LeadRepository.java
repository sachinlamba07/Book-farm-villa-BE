package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Lead;
import com.bookfarmvilla.entity.enums.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeadRepository extends JpaRepository<Lead, String> {

    // The entity graph loads property and vendor in the same query (no N+1).
    @EntityGraph(attributePaths = { "property", "vendor" })
    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "property", "vendor" })
    Page<Lead> findAll(Pageable pageable);

    /** Searches lead details and the linked property or vendor name. */
    @EntityGraph(attributePaths = { "property", "vendor" })
    @Query(value = """
            select l from Lead l
            left join l.property p
            left join l.vendor v
            where (:status is null or l.status = :status)
              and (lower(l.name) like :pattern
                   or lower(l.phone) like :pattern
                   or lower(coalesce(l.email, '')) like :pattern
                   or lower(coalesce(l.eventType, '')) like :pattern
                   or lower(coalesce(l.message, '')) like :pattern
                   or lower(coalesce(p.title, '')) like :pattern
                   or lower(coalesce(p.originalPropertyName, '')) like :pattern
                   or lower(coalesce(v.name, '')) like :pattern)
            """, countQuery = """
            select count(l) from Lead l
            left join l.property p
            left join l.vendor v
            where (:status is null or l.status = :status)
              and (lower(l.name) like :pattern
                   or lower(l.phone) like :pattern
                   or lower(coalesce(l.email, '')) like :pattern
                   or lower(coalesce(l.eventType, '')) like :pattern
                   or lower(coalesce(l.message, '')) like :pattern
                   or lower(coalesce(p.title, '')) like :pattern
                   or lower(coalesce(p.originalPropertyName, '')) like :pattern
                   or lower(coalesce(v.name, '')) like :pattern)
            """)
    Page<Lead> search(
            @Param("status") LeadStatus status,
            @Param("pattern") String pattern,
            Pageable pageable);
}