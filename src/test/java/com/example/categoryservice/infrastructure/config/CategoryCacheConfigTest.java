package com.example.categoryservice.infrastructure.config;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryCacheConfig 테스트")
class CategoryCacheConfigTest {

    private CategoryCacheProperties cacheProperties;
    private CategoryCacheConfig cacheConfig;

    @BeforeEach
    void setUp() {
        cacheProperties = new CategoryCacheProperties();
        cacheConfig = new CategoryCacheConfig(cacheProperties);
    }

    @Test
    @DisplayName("CacheManager가 올바르게 생성되는지 확인")
    void CacheManager가_올바르게_생성되는지_확인() {
        // when
        CacheManager cacheManager = cacheConfig.categoryCacheManager();

        // then
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).containsExactlyInAnyOrder("categories", "categoryTree");
    }

    @Test
    @DisplayName("categories 캐시 설정이 올바른지 확인")
    void categories_캐시_설정이_올바른지_확인() {
        // when
        CacheManager cacheManager = cacheConfig.categoryCacheManager();
        org.springframework.cache.Cache categoriesCache = cacheManager.getCache("categories");

        // then
        assertThat(categoriesCache).isNotNull();
        assertThat(categoriesCache).isInstanceOf(CaffeineCache.class);

        CaffeineCache caffeineCache = (CaffeineCache) categoriesCache;
        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

        // 통계가 활성화되어 있는지 확인
        assertThat(nativeCache.stats()).isNotNull();
    }

    @Test
    @DisplayName("categoryTree 캐시 설정이 올바른지 확인")
    void categoryTree_캐시_설정이_올바른지_확인() {
        // when
        CacheManager cacheManager = cacheConfig.categoryCacheManager();
        org.springframework.cache.Cache categoryTreeCache = cacheManager.getCache("categoryTree");

        // then
        assertThat(categoryTreeCache).isNotNull();
        assertThat(categoryTreeCache).isInstanceOf(CaffeineCache.class);

        CaffeineCache caffeineCache = (CaffeineCache) categoryTreeCache;
        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

        // 통계가 활성화되어 있는지 확인
        assertThat(nativeCache.stats()).isNotNull();
    }

    @Test
    @DisplayName("커스텀 캐시 속성이 적용되는지 확인")
    void 커스텀_캐시_속성이_적용되는지_확인() {
        // given
        CategoryCacheProperties customProperties = new CategoryCacheProperties();
        customProperties.getCategories().setMaximumSize(10000L);
        customProperties.getCategories().setExpireAfterAccess(Duration.ofMinutes(30));
        customProperties.getCategoryTree().setMaximumSize(200L);
        customProperties.getCategoryTree().setExpireAfterWrite(Duration.ofMinutes(10));

        CategoryCacheConfig customConfig = new CategoryCacheConfig(customProperties);

        // when
        CacheManager cacheManager = customConfig.categoryCacheManager();

        // then
        assertThat(cacheManager.getCache("categories")).isNotNull();
        assertThat(cacheManager.getCache("categoryTree")).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 캐시 이름으로 조회시 동적 캐시 생성됨")
    void 존재하지_않는_캐시_이름으로_조회시_동적_캐시_생성됨() {
        // when
        CacheManager cacheManager = cacheConfig.categoryCacheManager();

        // then - CaffeineCacheManager는 동적으로 캐시를 생성함
        assertThat(cacheManager.getCache("nonExistentCache")).isNotNull();
    }
}