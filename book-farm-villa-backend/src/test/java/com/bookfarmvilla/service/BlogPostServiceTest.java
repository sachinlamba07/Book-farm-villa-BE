package com.bookfarmvilla.service;

import com.bookfarmvilla.dto.request.BlogPostRequest;
import com.bookfarmvilla.dto.response.BlogPostResponse;
import com.bookfarmvilla.entity.BlogPost;
import com.bookfarmvilla.entity.enums.BlogPostStatus;
import com.bookfarmvilla.exception.BadRequestException;
import com.bookfarmvilla.exception.ResourceNotFoundException;
import com.bookfarmvilla.repository.BlogPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {

    @Mock
    private BlogPostRepository blogPostRepository;

    @InjectMocks
    private BlogPostService blogPostService;

    @Test
    void createsDraftAndGeneratesSlugFromTitle() {
        when(blogPostRepository.findBySlug("best-farm-stays")).thenReturn(Optional.empty());
        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogPostResponse created = blogPostService.create(request("Best Farm Stays", null));

        assertEquals("best-farm-stays", created.slug());
        assertEquals(BlogPostStatus.DRAFT, created.status());
        assertEquals("Markdown body", created.content());
    }

    @Test
    void addsSuffixWhenGeneratedSlugIsAlreadyTaken() {
        BlogPost existing = new BlogPost();
        existing.setId("existing-id");
        when(blogPostRepository.findBySlug("best-farm-stays")).thenReturn(Optional.of(existing));
        when(blogPostRepository.findBySlug("best-farm-stays-2")).thenReturn(Optional.empty());
        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogPostResponse created = blogPostService.create(request("Best Farm Stays", null));

        assertEquals("best-farm-stays-2", created.slug());
    }

    @Test
    void rejectsAnAlreadyUsedCustomSlug() {
        BlogPost existing = new BlogPost();
        existing.setId("existing-id");
        when(blogPostRepository.findBySlug("custom-slug")).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class,
                () -> blogPostService.create(request("A title", "custom slug")));
    }

    @Test
    void publishingSetsStatusAndPublicationTime() {
        BlogPost draft = new BlogPost();
        draft.setId("post-id");
        draft.setSlug("draft-post");
        draft.setTitle("Draft post");
        draft.setContent("Markdown body");
        draft.setAuthorName("Team");
        when(blogPostRepository.findById("post-id")).thenReturn(Optional.of(draft));
        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogPostResponse published = blogPostService.publish("post-id");

        assertEquals(BlogPostStatus.PUBLISHED, published.status());
        assertNotNull(published.publishedAt());
        verify(blogPostRepository).save(draft);
    }

    @Test
    void doesNotAllowChangingPublishedSlug() {
        BlogPost published = new BlogPost();
        published.setId("post-id");
        published.setSlug("stable-slug");
        published.setStatus(BlogPostStatus.PUBLISHED);
        when(blogPostRepository.findById("post-id")).thenReturn(Optional.of(published));

        assertThrows(BadRequestException.class,
                () -> blogPostService.update("post-id", request("Updated title", "new-slug")));
    }

    @Test
    void publicSlugLookupRequiresPublishedStatus() {
        when(blogPostRepository.findBySlugAndStatus("draft-post", BlogPostStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> blogPostService.findPublishedBySlug("draft-post"));
        verify(blogPostRepository).findBySlugAndStatus("draft-post", BlogPostStatus.PUBLISHED);
    }

    private BlogPostRequest request(String title, String slug) {
        return new BlogPostRequest(
                title, slug, "An excerpt", "Markdown body",
                "https://example.com/cover.jpg", "Cover image", "Travel",
                "Book Farm Villa", null, null
        );
    }
}
