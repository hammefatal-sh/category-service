package com.example.categoryservice.application.service;

import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;
import com.example.categoryservice.domain.repository.CategoryRepository;
import com.example.categoryservice.infrastructure.config.CategoryCacheConfig;
import com.example.categoryservice.infrastructure.config.CategoryCacheProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {CategoryService.class, CategoryMapper.class, CategoryCacheConfig.class, CategoryCacheProperties.class})
@ActiveProfiles("test")
@DisplayName("CategoryService 캐시 테스트")
class CategoryServiceCacheTest {

    @Autowired
    private CategoryService categoryService;

    @MockBean
    private CategoryRepository categoryRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // 캐시 초기화
        cacheManager.getCacheNames().forEach(cacheName ->
            cacheManager.getCache(cacheName).clear());
    }

    @Test
    @DisplayName("카테고리 조회시 캐시 적용 확인")
    void 카테고리_조회시_캐시_적용_확인() {
        // given
        CategoryId categoryId = new CategoryId(1L);
        Category category = Category.createRoot(categoryId, "전자제품", "전자제품 카테고리");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when - 첫 번째 호출
        CategoryResponse result1 = categoryService.getCategory(categoryId);

        // when - 두 번째 호출
        CategoryResponse result2 = categoryService.getCategory(categoryId);

        // then
        assertThat(result1).isEqualTo(result2);
        // Repository는 한 번만 호출되어야 함 (두 번째는 캐시에서)
        verify(categoryRepository, times(1)).findById(categoryId);

        // 캐시에 값이 저장되어 있는지 확인
        org.springframework.cache.Cache categoriesCache = cacheManager.getCache("categories");
        assertThat(categoriesCache.get(categoryId)).isNotNull();
    }

    @Test
    @DisplayName("카테고리 트리 조회시 캐시 적용 확인")
    void 카테고리_트리_조회시_캐시_적용_확인() {
        // given
        List<Category> categories = List.of(
            Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리")
        );

        when(categoryRepository.findAll()).thenReturn(categories);

        // when - 첫 번째 호출
        CategoryTreeResponse result1 = categoryService.getAllCategories();

        // when - 두 번째 호출
        CategoryTreeResponse result2 = categoryService.getAllCategories();

        // then
        assertThat(result1).isEqualTo(result2);
        // Repository는 한 번만 호출되어야 함 (두 번째는 캐시에서)
        verify(categoryRepository, times(1)).findAll();

        // 캐시에 값이 저장되어 있는지 확인
        org.springframework.cache.Cache categoryTreeCache = cacheManager.getCache("categoryTree");
        assertThat(categoryTreeCache.get("all")).isNotNull();
    }

    @Test
    @DisplayName("특정 카테고리 트리 조회시 캐시 적용 확인")
    void 특정_카테고리_트리_조회시_캐시_적용_확인() {
        // given
        CategoryId rootId = new CategoryId(1L);
        List<Category> categories = List.of(
            Category.createRoot(rootId, "전자제품", "전자제품 카테고리")
        );

        when(categoryRepository.existsById(rootId)).thenReturn(true);
        when(categoryRepository.findAll()).thenReturn(categories);

        // when - 첫 번째 호출
        CategoryTreeResponse result1 = categoryService.getCategoryTree(rootId);

        // when - 두 번째 호출
        CategoryTreeResponse result2 = categoryService.getCategoryTree(rootId);

        // then
        assertThat(result1).isEqualTo(result2);
        // Repository의 findAll은 한 번만 호출되어야 함
        verify(categoryRepository, times(1)).findAll();
        // existsById는 캐시되지 않으므로 두 번 호출됨
        verify(categoryRepository, times(2)).existsById(rootId);

        // 캐시에 값이 저장되어 있는지 확인
        org.springframework.cache.Cache categoryTreeCache = cacheManager.getCache("categoryTree");
        assertThat(categoryTreeCache.get("tree:" + rootId)).isNotNull();
    }

    @Test
    @DisplayName("캐시 통계 확인")
    void 캐시_통계_확인() {
        // given
        CategoryId categoryId = new CategoryId(1L);
        Category category = Category.createRoot(categoryId, "전자제품", "전자제품 카테고리");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when
        categoryService.getCategory(categoryId); // 첫 번째 호출 - cache miss
        categoryService.getCategory(categoryId); // 두 번째 호출 - cache hit

        // then
        org.springframework.cache.Cache categoriesCache = cacheManager.getCache("categories");
        assertThat(categoriesCache).isNotNull();

        // Caffeine 캐시의 경우 통계가 활성화되어 있는지 확인
        if (categoriesCache instanceof org.springframework.cache.caffeine.CaffeineCache) {
            org.springframework.cache.caffeine.CaffeineCache caffeineCache =
                (org.springframework.cache.caffeine.CaffeineCache) categoriesCache;

            com.github.benmanes.caffeine.cache.stats.CacheStats stats =
                caffeineCache.getNativeCache().stats();

            assertThat(stats.hitCount()).isGreaterThan(0);
            assertThat(stats.missCount()).isGreaterThan(0);
        }
    }
}