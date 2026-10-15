package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.BlogPostRequest;
import com.bookfarmvilla.dto.request.BlogPostStatusRequest;
import com.bookfarmvilla.dto.response.AdminBlogPostSummaryResponse;
import com.bookfarmvilla.dto.response.BlogPostResponse;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.entity.enums.BlogPostStatus;
import com.bookfarmvilla.service.BlogPostService;
import com.bookfarmvilla.util.ListSort;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin/blogs")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AdminBlogPostController {

    private static final Logger log = LoggerFactory.getLogger(AdminBlogPostController.class);
    private static final Set<String> SORT_FIELDS = Set.of("title", "status", "publishedAt", "createdAt", "modifiedAt");

    private final BlogPostService blogPostService;

    public AdminBlogPostController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminBlogPostSummaryResponse>> list(
            @RequestParam(required = false) BlogPostStatus status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Pageable sortedPageable = ListSort.apply(pageable, sortBy, sortDirection, SORT_FIELDS);
        return ResponseEntity.ok(blogPostService.findAll(status, sortedPageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogPostResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(blogPostService.findById(id));
    }

    @PostMapping
    public ResponseEntity<BlogPostResponse> create(@Valid @RequestBody BlogPostRequest request) {
        BlogPostResponse created = blogPostService.create(request);
        log.info("Created blog post id={}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogPostResponse> update(
            @PathVariable String id,
            @Valid @RequestBody BlogPostRequest request
    ) {
        BlogPostResponse updated = blogPostService.update(id, request);
        log.info("Updated blog post id={}", updated.id());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<BlogPostResponse> publish(@PathVariable String id) {
        BlogPostResponse published = blogPostService.publish(id);
        log.info("Published blog post id={}", published.id());
        return ResponseEntity.ok(published);
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<BlogPostResponse> archive(@PathVariable String id) {
        BlogPostResponse archived = blogPostService.archive(id);
        log.info("Archived blog post id={}", archived.id());
        return ResponseEntity.ok(archived);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BlogPostResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody BlogPostStatusRequest request
    ) {
        BlogPostResponse updated = blogPostService.updateStatus(id, request.status());
        log.info("Changed blog post id={} status={}", updated.id(), updated.status());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        blogPostService.delete(id);
        log.info("Deleted blog post id={}", id);
        return ResponseEntity.noContent().build();
    }
}
