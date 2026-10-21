package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.BlogPost;
import com.bookfarmvilla.entity.enums.BlogPostStatus;
import com.bookfarmvilla.dto.response.BlogPostSummaryResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, String>, JpaSpecificationExecutor<BlogPost> {

    Optional<BlogPost> findBySlugAndStatus(String slug, BlogPostStatus status);

    Optional<BlogPost> findBySlug(String slug);

    Page<BlogPost> findByStatus(BlogPostStatus status, Pageable pageable);

    @Query(value = """
            select new com.bookfarmvilla.dto.response.BlogPostSummaryResponse(
                b.id, b.slug, b.title, b.excerpt, b.coverImageUrl, b.coverImageAlt,
                b.category, b.authorName, b.publishedAt)
            from BlogPost b
            where b.status = :status
              and (:category is null or lower(b.category) = lower(:category))
              and (:keyword is null or lower(b.title) like concat('%', lower(:keyword), '%')
                   or lower(coalesce(b.excerpt, '')) like concat('%', lower(:keyword), '%'))
            """,
            countQuery = """
            select count(b) from BlogPost b
            where b.status = :status
              and (:category is null or lower(b.category) = lower(:category))
              and (:keyword is null or lower(b.title) like concat('%', lower(:keyword), '%')
                   or lower(coalesce(b.excerpt, '')) like concat('%', lower(:keyword), '%'))
            """)
    Page<BlogPostSummaryResponse> findPublishedSummaries(
            @Param("status") BlogPostStatus status,
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable);

}
