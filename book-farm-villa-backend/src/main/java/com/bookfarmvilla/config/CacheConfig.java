package com.bookfarmvilla.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Bounded instance-local caches; use shared cache only when running multiple
 * instances.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                cache("amenities", 256, Duration.ofMinutes(30)),
                cache("propertySearch", 256, Duration.ofSeconds(60)),
                cache("propertyV2Search", 256, Duration.ofSeconds(60)),
                cache("propertyCards", 256, Duration.ofSeconds(60)),
                cache("propertyDetails", 256, Duration.ofMinutes(5)),
                cache("propertyV2Details", 256, Duration.ofMinutes(5)),
                cache("propertyCities", 4, Duration.ofMinutes(30)),
                cache("vendorSearch", 128, Duration.ofSeconds(60)),
                cache("vendorDetails", 256, Duration.ofMinutes(5)),
                cache("vendorCities", 4, Duration.ofMinutes(30)),
                cache("publishedBlogs", 128, Duration.ofMinutes(5)),
                cache("publishedBlogDetails", 256, Duration.ofMinutes(5)),
                cache("propertyReviews", 256, Duration.ofMinutes(2)),
                cache("vendorReviews", 256, Duration.ofMinutes(2))));
        return manager;
    }

    private CaffeineCache cache(String name, long maximumSize, Duration ttl) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(ttl)
                .recordStats()
                .build());
    }
}