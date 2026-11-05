package com.bookfarmvilla.repository;

import com.bookfarmvilla.dto.response.PropertyCardRatingRow;
import com.bookfarmvilla.entity.Review;
import com.bookfarmvilla.entity.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByPropertyId(String propertyId);
    List<Review> findByVendorId(String vendorId);
    List<Review> findByPropertyIdAndStatusOrderByCreatedAtDesc(String propertyId, ReviewStatus status);
    List<Review> findByVendorIdAndStatusOrderByCreatedAtDesc(String vendorId, ReviewStatus status);

    @Query("""
            select new com.bookfarmvilla.dto.response.PropertyCardRatingRow(r.property.id, avg(r.rating))
            from Review r
            where r.property.id in :propertyIds
              and r.status = com.bookfarmvilla.entity.enums.ReviewStatus.APPROVED
            group by r.property.id
            """)
    List<PropertyCardRatingRow> findPropertyCardRatings(@Param("propertyIds") List<String> propertyIds);

    @Query("""
            select r from Review r
            left join r.property p
            left join r.vendor v
            where (:propertyId is null or p.id = :propertyId)
              and (:vendorId is null or v.id = :vendorId)
              and (:keyword = ''
                   or lower(r.reviewerName) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(r.title, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(r.comment, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(r.bookingReference, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(p.title, '')) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(v.name, '')) like lower(concat('%', :keyword, '%')))
            """)
    Page<Review> search(
            @Param("keyword") String keyword,
            @Param("propertyId") String propertyId,
            @Param("vendorId") String vendorId,
            Pageable pageable
    );
}
