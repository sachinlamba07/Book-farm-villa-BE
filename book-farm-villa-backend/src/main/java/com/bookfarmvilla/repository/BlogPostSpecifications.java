package com.bookfarmvilla.repository;

import com.bookfarmvilla.entity.BlogPost;
import com.bookfarmvilla.entity.enums.BlogPostStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class BlogPostSpecifications {

    private BlogPostSpecifications() {
    }

    public static Specification<BlogPost> hasStatus(BlogPostStatus status) {
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<BlogPost> hasCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        String normalizedCategory = category.trim().toLowerCase(Locale.ROOT);
        return (root, query, builder) -> builder.equal(
                builder.lower(root.<String>get("category")),
                normalizedCategory
        );
    }

    public static Specification<BlogPost> titleOrExcerptContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.<String>get("title")), pattern),
                builder.like(builder.lower(builder.coalesce(root.<String>get("excerpt"), "")), pattern)
        );
    }
}
