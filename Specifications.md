# Category Service - 상세 개발 문서

## 프로젝트 개요

Category Service는 온라인 쇼핑몰의 상품 카테고리 관리를 위한 RESTful API 마이크로서비스입니다. Hexagonal Architecture(Port & Adapters) 패턴을 적용하여 설계되었으며, Spring Boot 3.2.0과 Java 17을 기반으로 구현되었습니다.

### 주요 기능
- 계층적 카테고리 트리 구조 관리
- CRUD 작업 (생성, 조회, 수정, 삭제)
- Caffeine 캐시를 활용한 고성능 조회
- 포괄적인 API 문서화 (OpenAPI/Swagger)
- 종합적인 모니터링 및 헬스체크
- Domain-Driven Design (DDD) 적용

### 기술 스택
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: H2 (Embedded)
- **Cache**: Caffeine
- **Documentation**: SpringDoc OpenAPI 3.0
- **Build Tool**: Gradle 8.12.1
- **Architecture**: Hexagonal Architecture

## 아키텍처 설계

### Hexagonal Architecture 구조

```
┌─────────────────────────────────────┐
│           Infrastructure            │
│  ┌─────────┐  ┌─────────┐  ┌──────┐│
│  │   Web   │  │ Cache   │  │  DB  ││
│  │ (REST)  │  │(Caffeine│  │ (H2) ││
│  └─────────┘  │)        │  └──────┘│
│               └─────────┘           │
├─────────────────────────────────────┤
│           Application               │
│  ┌─────────────────────────────────┐│
│  │        Use Cases                ││
│  │    (Category Service)           ││
│  └─────────────────────────────────┘│
├─────────────────────────────────────┤
│             Domain                  │
│  ┌──────────┐  ┌─────────────────┐ │
│  │Entities  │  │  Value Objects  │ │
│  │(Category)│  │  (CategoryId)   │ │
│  └──────────┘  └─────────────────┘ │
└─────────────────────────────────────┘
```

### 패키지 구조

```
src/main/java/com/example/categoryservice/
├── domain/
│   ├── model/
│   │   ├── Category.java           # 카테고리 엔티티
│   │   └── CategoryId.java         # 카테고리 ID 값 객체
│   ├── repository/
│   │   └── CategoryRepository.java  # 도메인 리포지토리 인터페이스
│   └── exception/
│       ├── CategoryNotFoundException.java
│       ├── InvalidCategoryException.java
│       └── CategoryHasChildrenException.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CategoryUseCase.java      # 입력 포트
│   │   │   ├── CreateCategoryCommand.java
│   │   │   └── UpdateCategoryCommand.java
│   │   └── out/
│   │       ├── CategoryResponse.java
│   │       ├── CategoryTreeResponse.java
│   │       └── CategoryNodeResponse.java
│   ├── service/
│   │   └── CategoryService.java     # 애플리케이션 서비스
│   └── mapper/
│       └── CategoryMapper.java      # 매퍼
├── infrastructure/
│   ├── web/
│   │   ├── CategoryController.java  # REST 컨트롤러
│   │   ├── request/
│   │   │   ├── CreateCategoryRequest.java
│   │   │   └── UpdateCategoryRequest.java
│   │   └── GlobalExceptionHandler.java
│   ├── persistence/
│   │   ├── CategoryRepositoryImpl.java   # 리포지토리 구현
│   │   └── CategoryJpaRepository.java    # JPA 리포지토리
│   ├── config/
│   │   ├── CategoryCacheConfig.java      # 캐시 설정
│   │   └── OpenApiConfig.java            # API 문서 설정
│   └── monitoring/
│       ├── CategoryServiceHealthIndicator.java
│       └── CategoryServiceInfoContributor.java
└── CategoryServiceApplication.java  # 메인 애플리케이션
```

## 상세 구현 내용

### 1. 도메인 레이어 (Domain Layer)

#### Category 엔티티
```java
@Entity
@Table(name = "categories")
public class Category {
    @EmbeddedId
    private CategoryId id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "parent_id"))
    private CategoryId parentId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

**주요 특징:**
- DDD 원칙에 따른 Rich Domain Model
- 비즈니스 로직과 validation을 도메인 내부에 캡슐화
- 불변성 보장을 위한 생성자 패턴 사용

#### CategoryId 값 객체
```java
@Embeddable
public class CategoryId implements Serializable {
    @Column(name = "id")
    private Long value;

    // equals, hashCode, toString 구현
}
```

### 2. 애플리케이션 레이어 (Application Layer)

#### CategoryService 유스케이스 구현
```java
@Service
@RequiredArgsConstructor
@Transactional
@CacheConfig(cacheManager = "categoryCache")
public class CategoryService implements CategoryUseCase {

    @Override
    @Cacheable(value = "categories", key = "#id.value")
    public CategoryResponse getCategory(CategoryId id) {
        // 구현 로직
    }

    @Override
    @Cacheable(value = "categoryTree", key = "'all'")
    public CategoryTreeResponse getAllCategories() {
        // 구현 로직
    }
}
```

**캐시 전략:**
- `categories`: 개별 카테고리 조회 캐시 (15분 유효)
- `categoryTree`: 트리 구조 캐시 (5분 유효)

### 3. 인프라스트럭처 레이어 (Infrastructure Layer)

#### REST API 컨트롤러
```java
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Categories", description = "카테고리 관리 API")
public class CategoryController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "카테고리 생성")
    public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        // 구현 로직
    }
}
```

**API 엔드포인트:**
- `POST /api/v1/categories` - 카테고리 생성
- `GET /api/v1/categories/{id}` - 카테고리 조회
- `GET /api/v1/categories` - 전체 카테고리 트리 조회
- `GET /api/v1/categories/{id}/tree` - 특정 카테고리 하위 트리 조회
- `PUT /api/v1/categories/{id}` - 카테고리 수정
- `DELETE /api/v1/categories/{id}` - 카테고리 삭제

## 빌드 및 실행 방법

### 1. 사전 요구사항
- **Java 17** 이상
- **Gradle 8.12.1** 이상

### 2. 프로젝트 클론 및 빌드
```bash
# 프로젝트 클론
git clone <repository-url>
cd category-service

# 프로젝트 빌드
./gradlew clean build

# 테스트 실행
./gradlew test

# 애플리케이션 실행
./gradlew bootRun
```

### 3. 빌드 설정 (build.gradle)
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

java {
    sourceCompatibility = '17'
}

dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // Cache
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    implementation 'com.github.ben-manes.caffeine:caffeine'

    // Database
    runtimeOnly 'com.h2database:h2'

    // Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

## 설정 방법

### 1. 애플리케이션 설정 (application.yml)
```yaml
spring:
  application:
    name: category-service
  datasource:
    url: jdbc:h2:mem:categorydb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  cache:
    type: caffeine

# 캐시 설정
app:
  cache:
    categories:
      maximum-size: 5000
      expire-after-access: PT15M
    category-tree:
      maximum-size: 100
      expire-after-write: PT5M

server:
  port: 8080

# API 문서화 설정
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

# 모니터링 설정
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus, configprops, env, loggers, httptrace, threaddump, heapdump
  endpoint:
    health:
      enabled: true
      show-details: always
    info:
      enabled: true
```

### 2. 캐시 설정
```java
@Configuration
@EnableCaching
@EnableConfigurationProperties(CategoryCacheProperties.class)
public class CategoryCacheConfig {

    @Bean("categoryCache")
    public CacheManager categoryCache(CategoryCacheProperties properties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // categories 캐시 설정
        cacheManager.registerCustomCache("categories",
            Caffeine.newBuilder()
                .maximumSize(properties.getCategories().getMaximumSize())
                .expireAfterAccess(properties.getCategories().getExpireAfterAccess())
                .recordStats()
                .build());

        return cacheManager;
    }
}
```

### 3. OpenAPI 문서화 설정
```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI categoryServiceOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .tags(tagList())
                .components(apiComponents())
                .externalDocs(externalDocumentation());
    }

    private Info apiInfo() {
        return new Info()
                .title("Category Service API")
                .description("온라인 쇼핑몰의 상품 카테고리 관리를 위한 RESTful API 서비스")
                .version("v1.0.0")
                .contact(new Contact()
                        .name("Development Team")
                        .email("dev@example.com")
                        .url("https://github.com/example/category-service"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
```

## 테스트 방법

### 1. 단위 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests CategoryServiceTest

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

### 2. 통합 테스트
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@Transactional
class CategoryControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void 카테고리_생성_성공() {
        CreateCategoryRequest request = new CreateCategoryRequest("전자제품", "전자제품 카테고리", null);

        ResponseEntity<CategoryResponse> response = restTemplate.postForEntity(
                "/api/v1/categories", request, CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("전자제품");
    }
}
```

### 3. 캐시 테스트
```java
@SpringBootTest
@EnableCaching
class CategoryServiceCacheTest {

    @Test
    void 카테고리_조회시_캐시_적용_확인() {
        CategoryId id = new CategoryId(1L);

        // 첫 번째 호출 - DB에서 조회
        CategoryResponse first = categoryService.getCategory(id);

        // 두 번째 호출 - 캐시에서 조회
        CategoryResponse second = categoryService.getCategory(id);

        // 캐시 통계 확인
        Cache cache = cacheManager.getCache("categories");
        CaffeineCache caffeineCache = (CaffeineCache) cache;
        assertThat(caffeineCache.getNativeCache().stats().hitCount()).isEqualTo(1);
    }
}
```

## API 사용 방법

### 1. Swagger UI 접근
```
http://localhost:8080/swagger-ui.html
```

### 2. API 문서 (JSON)
```
http://localhost:8080/api-docs
```

### 3. 주요 API 사용 예제

#### 카테고리 생성
```bash
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "전자제품",
    "description": "전자제품 카테고리",
    "parentId": null
  }'
```

**응답:**
```json
{
  "id": 1,
  "name": "전자제품",
  "description": "전자제품 카테고리",
  "parent_id": null,
  "created_at": "2025-01-01T10:00:00",
  "updated_at": "2025-01-01T10:00:00"
}
```

#### 전체 카테고리 트리 조회
```bash
curl http://localhost:8080/api/v1/categories
```

**응답:**
```json
{
  "categories": [
    {
      "id": 1,
      "name": "전자제품",
      "description": "전자제품 카테고리",
      "created_at": "2025-01-01T10:00:00",
      "updated_at": "2025-01-01T10:00:00",
      "children": [
        {
          "id": 2,
          "name": "스마트폰",
          "description": "스마트폰 카테고리",
          "created_at": "2025-01-01T10:00:00",
          "updated_at": "2025-01-01T10:00:00",
          "children": []
        }
      ]
    }
  ]
}
```

## 모니터링 및 헬스체크

### 1. Actuator 엔드포인트
```
http://localhost:8080/actuator
```

**사용 가능한 엔드포인트:**
- `/actuator/health` - 서비스 상태 확인
- `/actuator/info` - 애플리케이션 정보
- `/actuator/metrics` - 메트릭 정보
- `/actuator/prometheus` - Prometheus 메트릭
- `/actuator/configprops` - 설정 속성
- `/actuator/env` - 환경 변수
- `/actuator/loggers` - 로거 설정
- `/actuator/threaddump` - 스레드 덤프
- `/actuator/heapdump` - 힙 덤프

### 2. 헬스체크 확인
```bash
curl http://localhost:8080/actuator/health
```

**응답:**
```json
{
  "status": "UP",
  "components": {
    "categoryServiceHealth": {
      "status": "UP",
      "details": {
        "database": "UP",
        "cache": {
          "available": true,
          "cacheNames": ["categories", "categoryTree"],
          "details": {
            "categories": "UP",
            "categoryTree": "UP"
          }
        },
        "categoryCount": 5,
        "lastCheck": "2025-01-01T10:00:00"
      }
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "H2"
      }
    }
  }
}
```

### 3. 애플리케이션 정보 확인
```bash
curl http://localhost:8080/actuator/info
```

**주요 정보 포함:**
- 애플리케이션 메타데이터
- 빌드 정보
- Java 런타임 정보
- 캐시 통계
- 카테고리 통계
- 시스템 정보

### 4. 메트릭 확인
```bash
# 전체 메트릭 목록
curl http://localhost:8080/actuator/metrics

# 캐시 메트릭
curl http://localhost:8080/actuator/metrics/cache.gets

# JVM 메모리 메트릭
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

## 운영 환경 배포 가이드

### 1. 프로덕션 설정
```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:postgresql://prod-db:5432/categorydb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

management:
  endpoint:
    health:
      show-details: when-authorized
  endpoints:
    web:
      base-path: /management

logging:
  level:
    com.example.categoryservice: INFO
    org.springframework.web: WARN
```

### 2. Docker 설정
```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app
COPY build/libs/category-service-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3. Kubernetes 배포
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: category-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: category-service
  template:
    metadata:
      labels:
        app: category-service
    spec:
      containers:
      - name: category-service
        image: category-service:latest
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

## 성능 최적화

### 1. 캐시 최적화
- Caffeine 캐시 통계 모니터링
- 캐시 히트율 기반 설정 조정
- 메모리 사용량 최적화

### 2. 데이터베이스 최적화
- JPA 쿼리 최적화
- 인덱스 적용
- Connection Pool 설정

### 3. JVM 튜닝
```bash
java -Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar app.jar
```

## 트러블슈팅

### 1. 일반적인 문제

#### Bean 이름 충돌
```
ConflictingBeanDefinitionException: Annotation-specified bean name 'categoryService' conflicts
```
**해결방법:** @Component 어노테이션의 이름을 명시적으로 지정

#### 캐시 설정 문제
```
NoUniqueBeanDefinitionException: No qualifying bean of type 'CacheManager'
```
**해결방법:** 캐시 매니저 빈 이름을 명시적으로 지정하고 @Qualifier 사용

### 2. 로그 분석
```yaml
logging:
  level:
    com.example.categoryservice: DEBUG
    org.springframework.cache: DEBUG
    org.hibernate.SQL: DEBUG
```

### 3. 성능 모니터링
- `/actuator/metrics`를 통한 메트릭 수집
- 캐시 히트율 모니터링
- 응답 시간 측정

## 확장 가능성

### 1. 추가 기능 구현
- 카테고리 순서 관리
- 카테고리 이미지 업로드
- 다국어 지원
- 카테고리 검색 기능

### 2. 아키텍처 확장
- CQRS 패턴 적용
- Event Sourcing 구현
- 마이크로서비스 분리

### 3. 기술 스택 업그레이드
- Redis 캐시 연동
- PostgreSQL 데이터베이스 연동
- ElasticSearch 검색 엔진 연동

이 문서는 Category Service의 완전한 개발 및 운영 가이드를 제공합니다. 추가 질문이나 상세한 설명이 필요한 부분이 있다면 언제든 문의해 주세요.