package com.example.categoryservice.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryCacheProperties 테스트")
class CategoryCachePropertiesTest {

    @Test
    @DisplayName("기본 값으로 CategoryCacheProperties 생성 시 올바른 기본값 설정")
    void 기본_값으로_CategoryCacheProperties_생성시_올바른_기본값_설정() {
        // when
        CategoryCacheProperties properties = new CategoryCacheProperties();

        // then
        assertThat(properties.getCategories().getMaximumSize()).isEqualTo(5000L);
        assertThat(properties.getCategories().getExpireAfterAccess()).isEqualTo(Duration.ofMinutes(15));
        assertThat(properties.getCategories().getExpireAfterWrite()).isEqualTo(Duration.ofMinutes(5));

        assertThat(properties.getCategoryTree().getMaximumSize()).isEqualTo(100L);
        assertThat(properties.getCategoryTree().getExpireAfterAccess()).isEqualTo(Duration.ofMinutes(10));
        assertThat(properties.getCategoryTree().getExpireAfterWrite()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    @DisplayName("CacheSpec 기본값 테스트")
    void CacheSpec_기본값_테스트() {
        // when
        CategoryCacheProperties.CacheSpec cacheSpec = new CategoryCacheProperties.CacheSpec();

        // then
        assertThat(cacheSpec.getMaximumSize()).isEqualTo(1000L);
        assertThat(cacheSpec.getExpireAfterAccess()).isEqualTo(Duration.ofMinutes(10));
        assertThat(cacheSpec.getExpireAfterWrite()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    @DisplayName("CacheSpec 값 수정 가능")
    void CacheSpec_값_수정_가능() {
        // given
        CategoryCacheProperties.CacheSpec cacheSpec = new CategoryCacheProperties.CacheSpec();

        // when
        cacheSpec.setMaximumSize(2000L);
        cacheSpec.setExpireAfterAccess(Duration.ofMinutes(20));
        cacheSpec.setExpireAfterWrite(Duration.ofMinutes(10));

        // then
        assertThat(cacheSpec.getMaximumSize()).isEqualTo(2000L);
        assertThat(cacheSpec.getExpireAfterAccess()).isEqualTo(Duration.ofMinutes(20));
        assertThat(cacheSpec.getExpireAfterWrite()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("categories와 categoryTree 설정 독립성 확인")
    void categories와_categoryTree_설정_독립성_확인() {
        // given
        CategoryCacheProperties properties = new CategoryCacheProperties();

        // when
        properties.getCategories().setMaximumSize(10000L);
        properties.getCategoryTree().setMaximumSize(50L);

        // then
        assertThat(properties.getCategories().getMaximumSize()).isEqualTo(10000L);
        assertThat(properties.getCategoryTree().getMaximumSize()).isEqualTo(50L);
    }
}