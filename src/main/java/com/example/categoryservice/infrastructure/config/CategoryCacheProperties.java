package com.example.categoryservice.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "app.cache")
public class CategoryCacheProperties {

    private CacheSpec categories = new CacheSpec();
    private CacheSpec categoryTree = new CacheSpec();

    @Data
    public static class CacheSpec {
        private long maximumSize = 1000L;
        private Duration expireAfterAccess = Duration.ofMinutes(10);
        private Duration expireAfterWrite = Duration.ofMinutes(5);
    }

    public CategoryCacheProperties() {
        // Categories cache: 개별 카테고리 조회용 (자주 접근)
        categories.setMaximumSize(5000L);
        categories.setExpireAfterAccess(Duration.ofMinutes(15));

        // Category tree cache: 트리 구조 조회용 (상대적으로 적은 키, 빠른 만료)
        categoryTree.setMaximumSize(100L);
        categoryTree.setExpireAfterWrite(Duration.ofMinutes(5));
    }
}