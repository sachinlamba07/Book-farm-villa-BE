package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.request.BlogPostRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(blogPostService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogPostResponse> update(
            @PathVariable String id,
            @Valid @RequestBody BlogPostRequest request
    ) {
        return ResponseEntity.ok(blogPostService.update(id, request));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<BlogPostResponse> publish(@PathVariable String id) {
        return ResponseEntity.ok(blogPostService.publish(id));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<BlogPostResponse> archive(@PathVariable String id) {
        return ResponseEntity.ok(blogPostService.archive(id));
    }
}
