CREATE TABLE blog_posts (
    id                  VARCHAR(36) PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    slug                VARCHAR(255) NOT NULL UNIQUE,
    excerpt             VARCHAR(500),
    content             TEXT NOT NULL,
    cover_image_url     VARCHAR(1024),
    cover_image_alt     VARCHAR(255),
    category            VARCHAR(100),
    author_name         VARCHAR(255) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    seo_title           VARCHAR(255),
    seo_description     VARCHAR(500),
    published_at        TIMESTAMP,
    created_at          TIMESTAMP NOT NULL,
    modified_at         TIMESTAMP NOT NULL
);

CREATE INDEX idx_blog_posts_status_published_at ON blog_posts(status, published_at DESC);
CREATE INDEX idx_blog_posts_category ON blog_posts(category);
