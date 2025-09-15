package com.example.categoryservice.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Slf4j
@Configuration
@EnableCaching
@EnableConfigurationProperties(CategoryCacheProperties.class)
public class CategoryCacheConfig {

    private final CategoryCacheProperties cacheProperties;

    public CategoryCacheConfig(CategoryCacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    @Bean
    @Primary
    public CacheManager categoryCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Categories cache configuration
        cacheManager.registerCustomCache("categories",
            Caffeine.newBuilder()
                .maximumSize(cacheProperties.getCategories().getMaximumSize())
                .expireAfterAccess(cacheProperties.getCategories().getExpireAfterAccess())
                .recordStats()
                .build());

        // Category tree cache configuration
        cacheManager.registerCustomCache("categoryTree",
            Caffeine.newBuilder()
                .maximumSize(cacheProperties.getCategoryTree().getMaximumSize())
                .expireAfterWrite(cacheProperties.getCategoryTree().getExpireAfterWrite())
                .recordStats()
                .build());

        log.info("Category cache manager configured with properties: {}", cacheProperties);

        return cacheManager;
    }
}