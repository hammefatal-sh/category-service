package com.example.categoryservice.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CategoryCacheConfig 통합 테스트")
class CategoryCacheConfigIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CategoryCacheProperties cacheProperties;

    @Test
    @DisplayName("Spring Context에서 캐시 매니저가 올바르게 로드되는지 확인")
    void Spring_Context에서_캐시_매니저가_올바르게_로드되는지_확인() {
        // then
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).containsExactlyInAnyOrder("categories", "categoryTree");
    }

    @Test
    @DisplayName("Categories 캐시가 올바른 설정으로 생성되는지 확인")
    void Categories_캐시가_올바른_설정으로_생성되는지_확인() {
        // when
        org.springframework.cache.Cache categoriesCache = cacheManager.getCache("categories");

        // then
        assertThat(categoriesCache).isNotNull();
        assertThat(categoriesCache).isInstanceOf(CaffeineCache.class);
    }

    @Test
    @DisplayName("CategoryTree 캐시가 올바른 설정으로 생성되는지 확인")
    void CategoryTree_캐시가_올바른_설정으로_생성되는지_확인() {
        // when
        org.springframework.cache.Cache categoryTreeCache = cacheManager.getCache("categoryTree");

        // then
        assertThat(categoryTreeCache).isNotNull();
        assertThat(categoryTreeCache).isInstanceOf(CaffeineCache.class);
    }

    @Test
    @DisplayName("캐시 속성이 올바르게 로드되는지 확인")
    void 캐시_속성이_올바르게_로드되는지_확인() {
        // then
        assertThat(cacheProperties).isNotNull();
        assertThat(cacheProperties.getCategories().getMaximumSize()).isEqualTo(5000L);
        assertThat(cacheProperties.getCategoryTree().getMaximumSize()).isEqualTo(100L);
    }

    @Test
    @DisplayName("캐시가 기본적인 put/get 동작을 수행하는지 확인")
    void 캐시가_기본적인_put_get_동작을_수행하는지_확인() {
        // given
        org.springframework.cache.Cache categoriesCache = cacheManager.getCache("categories");

        // when
        categoriesCache.put("testKey", "testValue");
        String result = categoriesCache.get("testKey", String.class);

        // then
        assertThat(result).isEqualTo("testValue");
    }
}