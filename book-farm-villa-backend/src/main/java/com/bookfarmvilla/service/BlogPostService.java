package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.BlogPostRequest;
import com.bookfarmvilla.dto.response.AdminBlogPostSummaryResponse;
import com.bookfarmvilla.dto.response.BlogPostResponse;
import com.bookfarmvilla.dto.response.BlogPostSummaryResponse;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.entity.BlogPost;
import com.bookfarmvilla.entity.enums.BlogPostStatus;
import com.bookfarmvilla.exception.BadRequestException;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.BlogPostRepository;
import com.bookfarmvilla.util.BlogSlug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;

    public BlogPostService(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    public PageResponse<BlogPostSummaryResponse> findPublished(String category, String keyword, Pageable pageable) {
        Page<BlogPost> page = blogPostRepository.findPublished(
                BlogPostStatus.PUBLISHED, normalizeFilter(category), normalizeFilter(keyword), pageable
        );
        return PageResponse.from(page.map(this::toSummaryResponse));
    }

    public BlogPostResponse findPublishedBySlug(String slug) {
        BlogPost post = blogPostRepository.findBySlugAndStatus(slug, BlogPostStatus.PUBLISHED)
                .orElseThrow(() -> ResourceNotFoundException.of("Blog post", slug));
        return toResponse(post);
    }

    public PageResponse<AdminBlogPostSummaryResponse> findAll(BlogPostStatus status, Pageable pageable) {
        Page<BlogPost> page = status == null
                ? blogPostRepository.findAll(pageable)
                : blogPostRepository.findByStatus(status, pageable);
        return PageResponse.from(page.map(this::toAdminSummaryResponse));
    }

    public BlogPostResponse findById(String id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public BlogPostResponse create(BlogPostRequest request) {
        BlogPost post = new BlogPost();
        applyRequest(post, request, true);
        post.setStatus(BlogPostStatus.DRAFT);
        return toResponse(blogPostRepository.save(post));
    }

    @Transactional
    public BlogPostResponse update(String id, BlogPostRequest request) {
        BlogPost post = getOrThrow(id);
        applyRequest(post, request, false);
        return toResponse(blogPostRepository.save(post));
    }

    @Transactional
    public BlogPostResponse publish(String id) {
        BlogPost post = getOrThrow(id);
        if (post.getStatus() != BlogPostStatus.PUBLISHED) {
            post.setStatus(BlogPostStatus.PUBLISHED);
            post.setPublishedAt(Instant.now());
        }
        return toResponse(blogPostRepository.save(post));
    }

    @Transactional
    public BlogPostResponse archive(String id) {
        BlogPost post = getOrThrow(id);
        post.setStatus(BlogPostStatus.ARCHIVED);
        return toResponse(blogPostRepository.save(post));
    }

    private void applyRequest(BlogPost post, BlogPostRequest request, boolean creating) {
        String requestedSlug = request.slug();
        String slug;
        if (!creating && post.getStatus() == BlogPostStatus.PUBLISHED) {
            slug = post.getSlug();
            if (requestedSlug != null && !requestedSlug.isBlank()
                    && !BlogSlug.normalize(requestedSlug).equals(slug)) {
                throw new BadRequestException("A published blog post slug cannot be changed");
            }
        } else {
            String baseSlug = requestedSlug == null || requestedSlug.isBlank()
                    ? BlogSlug.fromTitle(request.title())
                    : BlogSlug.normalize(requestedSlug);
            if (baseSlug.isBlank()) {
                throw new BadRequestException("Slug must contain at least one letter or number");
            }
            slug = resolveAvailableSlug(baseSlug, post.getId(), requestedSlug == null || requestedSlug.isBlank());
        }

        post.setSlug(slug);
        post.setTitle(request.title().trim());
        post.setExcerpt(blankToNull(request.excerpt()));
        post.setContent(request.content());
        post.setCoverImageUrl(blankToNull(request.coverImageUrl()));
        post.setCoverImageAlt(blankToNull(request.coverImageAlt()));
        post.setCategory(blankToNull(request.category()));
        post.setAuthorName(request.authorName().trim());
        post.setSeoTitle(blankToNull(request.seoTitle()));
        post.setSeoDescription(blankToNull(request.seoDescription()));
    }

    private String resolveAvailableSlug(String baseSlug, String currentId, boolean generated) {
        String candidate = baseSlug;
        int suffix = 2;
        while (true) {
            BlogPost existing = blogPostRepository.findBySlug(candidate).orElse(null);
            if (existing == null || existing.getId().equals(currentId)) {
                return candidate;
            }
            if (!generated) {
                throw new BadRequestException("Blog post slug is already in use");
            }
            candidate = BlogSlug.withSuffix(baseSlug, suffix++);
        }
    }

    private BlogPost getOrThrow(String id) {
        return blogPostRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Blog post", id));
    }

    private BlogPostSummaryResponse toSummaryResponse(BlogPost post) {
        return new BlogPostSummaryResponse(
                post.getId(), post.getSlug(), post.getTitle(), post.getExcerpt(), post.getCoverImageUrl(),
                post.getCoverImageAlt(), post.getCategory(), post.getAuthorName(), post.getPublishedAt()
        );
    }

    private AdminBlogPostSummaryResponse toAdminSummaryResponse(BlogPost post) {
        return new AdminBlogPostSummaryResponse(
                post.getId(), post.getSlug(), post.getTitle(), post.getExcerpt(), post.getCoverImageUrl(),
                post.getCategory(), post.getAuthorName(), post.getStatus(), post.getPublishedAt(),
                post.getCreatedAt(), post.getModifiedAt()
        );
    }

    private BlogPostResponse toResponse(BlogPost post) {
        return new BlogPostResponse(
                post.getId(), post.getSlug(), post.getTitle(), post.getExcerpt(), post.getContent(),
                post.getCoverImageUrl(), post.getCoverImageAlt(), post.getCategory(), post.getAuthorName(),
                post.getStatus(), post.getSeoTitle(), post.getSeoDescription(), post.getPublishedAt(),
                post.getCreatedAt(), post.getModifiedAt()
        );
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
