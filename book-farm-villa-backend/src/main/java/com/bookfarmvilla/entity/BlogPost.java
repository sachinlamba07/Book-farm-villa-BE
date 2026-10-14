package com.bookfarmvilla.entity;

import com.bookfarmvilla.entity.enums.BlogPostStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "blog_posts")
@Getter
@Setter
@NoArgsConstructor
public class BlogPost extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 500)
    private String excerpt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "cover_image_url", length = 1024)
    private String coverImageUrl;

    @Column(name = "cover_image_alt")
    private String coverImageAlt;

    @Column(length = 100)
    private String category;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlogPostStatus status = BlogPostStatus.DRAFT;

    @Column(name = "seo_title")
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "published_at")
    private Instant publishedAt;
}
