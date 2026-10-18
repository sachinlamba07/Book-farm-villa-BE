package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.BlogPost;
import com.bookfarmvilla.entity.enums.BlogPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, String>, JpaSpecificationExecutor<BlogPost> {

    Optional<BlogPost> findBySlugAndStatus(String slug, BlogPostStatus status);

    Optional<BlogPost> findBySlug(String slug);

    Page<BlogPost> findByStatus(BlogPostStatus status, Pageable pageable);
}
