package com.bookfarmvilla.controller;

import com.bookfarmvilla.dto.response.BlogPostResponse;
import com.bookfarmvilla.dto.response.BlogPostSummaryResponse;
import com.bookfarmvilla.dto.response.PageResponse;
import com.bookfarmvilla.service.BlogPostService;
import com.bookfarmvilla.util.ListSort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blogs")
public class BlogPostController {

    private static final Set<String> SORT_FIELDS = Set.of("title", "publishedAt", "createdAt");

    private final BlogPostService blogPostService;

    public BlogPostController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<BlogPostSummaryResponse>> listPublished(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @PageableDefault(size = 12, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Pageable sortedPageable = ListSort.apply(pageable, sortBy, sortDirection, SORT_FIELDS);
        return ResponseEntity.ok(blogPostService.findPublished(category, keyword, sortedPageable));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<BlogPostResponse> getPublishedBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(blogPostService.findPublishedBySlug(slug));
    }
}
