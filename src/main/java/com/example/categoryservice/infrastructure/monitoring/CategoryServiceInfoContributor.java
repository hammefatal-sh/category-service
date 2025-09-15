package com.example.categoryservice.infrastructure.monitoring;

import com.example.categoryservice.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Category Service의 동적 정보를 제공하는 Info Contributor
 * 실시간 통계 정보와 운영 관련 정보를 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryServiceInfoContributor implements InfoContributor {

    private final CategoryRepository categoryRepository;
    private final CacheManager cacheManager;

    @Override
    public void contribute(Info.Builder builder) {
        try {
            Map<String, Object> categoryServiceInfo = new HashMap<>();

            // 카테고리 통계 정보
            addCategoryStatistics(categoryServiceInfo);

            // 캐시 통계 정보
            addCacheStatistics(categoryServiceInfo);

            // 시스템 정보
            addSystemInfo(categoryServiceInfo);

            builder.withDetail("categoryService", categoryServiceInfo);

        } catch (Exception e) {
            log.error("Failed to contribute category service info", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("status", "ERROR");
            errorInfo.put("message", "Failed to gather category service information");
            errorInfo.put("error", e.getMessage());
            builder.withDetail("categoryService", errorInfo);
        }
    }

    private void addCategoryStatistics(Map<String, Object> info) {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 전체 카테고리 수
            long totalCategories = categoryRepository.count();
            stats.put("totalCategories", totalCategories);

            // 루트 카테고리 수
            long rootCategories = categoryRepository.countRootCategories();
            stats.put("rootCategories", rootCategories);

            // 하위 카테고리 수
            stats.put("childCategories", totalCategories - rootCategories);

            // 평균 트리 깊이 등 추가 통계는 필요시 구현
            stats.put("averageTreeDepth", calculateAverageTreeDepth());

            info.put("statistics", stats);

        } catch (Exception e) {
            log.warn("Failed to gather category statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", "Failed to gather category statistics");
            info.put("statistics", errorStats);
        }
    }

    private void addCacheStatistics(Map<String, Object> info) {
        try {
            Map<String, Object> cacheInfo = new HashMap<>();

            if (cacheManager != null) {
                cacheInfo.put("cacheManager", cacheManager.getClass().getSimpleName());
                cacheInfo.put("cacheNames", cacheManager.getCacheNames());

                // 캐시별 통계 (Caffeine인 경우)
                Map<String, Object> cacheStats = new HashMap<>();
                for (String cacheName : cacheManager.getCacheNames()) {
                    try {
                        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                        if (cache instanceof org.springframework.cache.caffeine.CaffeineCache) {
                            org.springframework.cache.caffeine.CaffeineCache caffeineCache =
                                    (org.springframework.cache.caffeine.CaffeineCache) cache;

                            com.github.benmanes.caffeine.cache.stats.CacheStats stats =
                                    caffeineCache.getNativeCache().stats();

                            Map<String, Object> cacheStat = new HashMap<>();
                            cacheStat.put("hitCount", stats.hitCount());
                            cacheStat.put("missCount", stats.missCount());
                            cacheStat.put("hitRate", String.format("%.2f%%", stats.hitRate() * 100));
                            cacheStat.put("evictionCount", stats.evictionCount());
                            cacheStat.put("requestCount", stats.requestCount());

                            cacheStats.put(cacheName, cacheStat);
                        }
                    } catch (Exception e) {
                        cacheStats.put(cacheName, "Stats unavailable");
                    }
                }
                cacheInfo.put("statistics", cacheStats);
            } else {
                cacheInfo.put("status", "Cache manager not available");
            }

            info.put("cache", cacheInfo);

        } catch (Exception e) {
            log.warn("Failed to gather cache statistics", e);
            Map<String, Object> errorCache = new HashMap<>();
            errorCache.put("error", "Failed to gather cache statistics");
            info.put("cache", errorCache);
        }
    }

    private void addSystemInfo(Map<String, Object> info) {
        try {
            Map<String, Object> systemInfo = new HashMap<>();

            // 현재 시간
            systemInfo.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // JVM 메모리 정보
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> memoryInfo = new HashMap<>();
            memoryInfo.put("totalMemory", runtime.totalMemory());
            memoryInfo.put("freeMemory", runtime.freeMemory());
            memoryInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
            memoryInfo.put("maxMemory", runtime.maxMemory());
            systemInfo.put("memory", memoryInfo);

            // 시스템 프로퍼티
            Map<String, Object> systemProps = new HashMap<>();
            systemProps.put("javaVersion", System.getProperty("java.version"));
            systemProps.put("javaVendor", System.getProperty("java.vendor"));
            systemProps.put("osName", System.getProperty("os.name"));
            systemProps.put("osVersion", System.getProperty("os.version"));
            systemInfo.put("system", systemProps);

            info.put("runtime", systemInfo);

        } catch (Exception e) {
            log.warn("Failed to gather system info", e);
            Map<String, Object> errorSystem = new HashMap<>();
            errorSystem.put("error", "Failed to gather system information");
            info.put("runtime", errorSystem);
        }
    }

    private double calculateAverageTreeDepth() {
        // 실제 구현에서는 더 정교한 트리 깊이 계산이 필요할 수 있음
        // 현재는 간단한 추정치 반환
        try {
            long totalCategories = categoryRepository.count();
            long rootCategories = categoryRepository.countRootCategories();

            if (rootCategories == 0) return 0.0;
            if (totalCategories == rootCategories) return 1.0;

            // 간단한 추정: (전체 카테고리 수 / 루트 카테고리 수)
            return (double) totalCategories / rootCategories;

        } catch (Exception e) {
            log.warn("Failed to calculate average tree depth", e);
            return -1.0;
        }
    }
}