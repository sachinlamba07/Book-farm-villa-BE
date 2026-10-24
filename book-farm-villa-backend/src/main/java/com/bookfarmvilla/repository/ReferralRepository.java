package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.Referral;
import com.bookfarmvilla.entity.enums.ReferralStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ReferralRepository extends JpaRepository<Referral, String> {

    @Query("SELECT r FROM Referral r WHERE r.status = :status ORDER BY r.createdAt DESC")
    Page<Referral> findByStatus(@Param("status") ReferralStatus status, Pageable pageable);

    Optional<Referral> findByReferralNumber(String referralNumber);

    @Query(value = "SELECT MAX(CAST(SUBSTRING(r.referral_number, 14) AS INTEGER)) FROM referrals r WHERE r.referral_number LIKE CONCAT('BFV-REF-', ?1, '-%')", nativeQuery = true)
    Integer findMaxSequenceByYear(int year);

    @Query("SELECT r FROM Referral r WHERE " +
            "(LOWER(r.referrerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.refereeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.referrerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.refereePhone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Referral> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}