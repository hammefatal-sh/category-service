package com.example.categoryservice.infrastructure.monitoring;

import com.example.categoryservice.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Category Service 전용 Health Indicator
 * 서비스의 핵심 구성요소들의 상태를 점검합니다.
 */
@Slf4j
@Component("categoryServiceHealth")
@RequiredArgsConstructor
public class CategoryServiceHealthIndicator implements HealthIndicator {

    private final CategoryRepository categoryRepository;
    private final CacheManager cacheManager;

    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();

            // 데이터베이스 연결 상태 확인
            boolean dbHealthy = checkDatabaseHealth();
            details.put("database", dbHealthy ? "UP" : "DOWN");

            // 캐시 상태 확인
            Map<String, Object> cacheHealth = checkCacheHealth();
            details.put("cache", cacheHealth);

            // 카테고리 개수 확인
            long categoryCount = getCategoryCount();
            details.put("categoryCount", categoryCount);

            // 마지막 체크 시간
            details.put("lastCheck", LocalDateTime.now().toString());

            // 전체 상태 판단
            boolean isHealthy = dbHealthy && (Boolean) cacheHealth.get("available");

            if (isHealthy) {
                return Health.up()
                        .withDetails(details)
                        .build();
            } else {
                return Health.down()
                        .withDetails(details)
                        .build();
            }

        } catch (Exception e) {
            log.error("Health check failed", e);
            return Health.down()
                    .withException(e)
                    .withDetail("error", "Health check execution failed")
                    .withDetail("lastCheck", LocalDateTime.now().toString())
                    .build();
        }
    }

    private boolean checkDatabaseHealth() {
        try {
            // 간단한 카운트 쿼리로 DB 연결 확인
            categoryRepository.count();
            return true;
        } catch (Exception e) {
            log.warn("Database health check failed", e);
            return false;
        }
    }

    private Map<String, Object> checkCacheHealth() {
        Map<String, Object> cacheInfo = new HashMap<>();

        try {
            // 캐시 매니저 상태 확인
            boolean cacheAvailable = cacheManager != null;
            cacheInfo.put("available", cacheAvailable);

            if (cacheAvailable) {
                // 캐시 이름들 확인
                cacheInfo.put("cacheNames", cacheManager.getCacheNames());

                // 각 캐시의 상태 확인
                Map<String, Object> cacheDetails = new HashMap<>();
                for (String cacheName : cacheManager.getCacheNames()) {
                    try {
                        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                        cacheDetails.put(cacheName, cache != null ? "UP" : "DOWN");
                    } catch (Exception e) {
                        cacheDetails.put(cacheName, "ERROR");
                    }
                }
                cacheInfo.put("details", cacheDetails);
            }

        } catch (Exception e) {
            log.warn("Cache health check failed", e);
            cacheInfo.put("available", false);
            cacheInfo.put("error", e.getMessage());
        }

        return cacheInfo;
    }

    private long getCategoryCount() {
        try {
            return categoryRepository.count();
        } catch (Exception e) {
            log.warn("Failed to get category count", e);
            return -1;
        }
    }
}