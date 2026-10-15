package com.bookfarmvilla.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogPostRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must be at most 255 characters")
        String title,

        @Size(max = 255, message = "Slug must be at most 255 characters")
        String slug,

        @Size(max = 500, message = "Excerpt must be at most 500 characters")
        String excerpt,

        @NotBlank(message = "Content is required")
        String content,

        @Size(max = 1024, message = "Cover image URL must be at most 1024 characters")
        String coverImageUrl,

        @Size(max = 255, message = "Cover image alt text must be at most 255 characters")
        String coverImageAlt,

        @Size(max = 100, message = "Category must be at most 100 characters")
        String category,

        @NotBlank(message = "Author name is required")
        @Size(max = 255, message = "Author name must be at most 255 characters")
        String authorName,

        @Size(max = 255, message = "SEO title must be at most 255 characters")
        String seoTitle,

        @Size(max = 500, message = "SEO description must be at most 500 characters")
        String seoDescription
) {
}
