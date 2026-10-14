package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.BlogPost;
import com.bookfarmvilla.entity.enums.BlogPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, String> {

    Optional<BlogPost> findBySlugAndStatus(String slug, BlogPostStatus status);

    Optional<BlogPost> findBySlug(String slug);

    @Query("""
            SELECT post FROM BlogPost post
            WHERE post.status = :status
              AND (:category IS NULL OR LOWER(post.category) = LOWER(:category))
              AND (:keyword IS NULL OR
                   LOWER(post.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(post.excerpt) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<BlogPost> findPublished(
            @Param("status") BlogPostStatus status,
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Page<BlogPost> findByStatus(BlogPostStatus status, Pageable pageable);
}
