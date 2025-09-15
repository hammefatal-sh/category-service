# Detailed Design - Category Service

## 1. 아키텍처 개요

### 1.1 Hexagonal Architecture 구조
```
category-service/
├── src/main/java/com/example/categoryservice/
│   ├── application/           # Application Layer (Use Cases)
│   │   ├── port/             # Input/Output Ports
│   │   └── service/          # Application Services
│   ├── domain/               # Domain Layer (Business Logic)
│   │   ├── model/            # Domain Models
│   │   ├── repository/       # Repository Interfaces
│   │   └── exception/        # Domain Exceptions
│   ├── infrastructure/       # Infrastructure Layer (Adapters)
│   │   ├── persistence/      # Database Adapters
│   │   ├── web/             # Web Adapters (REST Controllers)
│   │   └── config/          # Configuration
│   └── CategoryServiceApplication.java
└── src/test/                 # Test Files
```

### 1.2 레이어별 책임
- **Domain Layer**: 비즈니스 로직, 도메인 모델, 비즈니스 규칙
- **Application Layer**: 유스케이스 조정, 트랜잭션 관리
- **Infrastructure Layer**: 외부 시스템과의 통신 (DB, Web)

## 2. 도메인 모델 설계

### 2.1 Category 엔티티
```java
// 단계별 구현을 위한 Category 도메인 모델 구조
public class Category {
    private CategoryId id;
    private String name;
    private String description;
    private CategoryId parentId;
    private List<Category> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 비즈니스 메서드들
    public void addChild(Category child);
    public void removeChild(CategoryId childId);
    public boolean isRoot();
    public boolean hasChildren();
    public List<Category> getAllDescendants();
}
```

### 2.2 Value Objects
```java
public class CategoryId {
    private final Long value;
}

public class CategoryTree {
    private final List<Category> roots;
    private final Map<CategoryId, Category> categoryMap;
}
```

## 3. Step-by-Step 구현 가이드

### Step 1: 프로젝트 초기 설정
**목표**: 기본 Spring Boot 프로젝트 구조와 의존성 설정

#### 1.1 build.gradle 설정
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
    implementation 'com.github.ben-manes.caffeine:caffeine'
    runtimeOnly 'com.h2database:h2'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

#### 1.2 application.yml 설정
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:categorydb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  h2:
    console:
      enabled: true
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterAccess=300s

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### Step 2: 도메인 레이어 구현
**목표**: 핵심 비즈니스 로직과 도메인 모델 구현

#### 2.1 CategoryId Value Object 구현
```java
@Embeddable
public class CategoryId {
    @Column(name = "id")
    private Long value;

    // 생성자, equals, hashCode, toString 구현
}
```

#### 2.2 Category 도메인 엔티티 구현
```java
@Entity
@Table(name = "categories")
public class Category {
    @EmbeddedId
    private CategoryId id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "parent_id"))
    private CategoryId parentId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 비즈니스 메서드 구현
    public boolean isRoot() {
        return parentId == null;
    }

    public void updateInfo(String name, String description) {
        validateName(name);
        this.name = name;
        this.description = description;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
    }
}
```

#### 2.3 도메인 예외 구현
```java
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(CategoryId categoryId) {
        super("Category not found: " + categoryId.getValue());
    }
}

public class CategoryHasChildrenException extends RuntimeException {
    public CategoryHasChildrenException(CategoryId categoryId) {
        super("Cannot delete category with children: " + categoryId.getValue());
    }
}

public class CircularReferenceException extends RuntimeException {
    public CircularReferenceException(CategoryId categoryId, CategoryId parentId) {
        super("Circular reference detected: " + categoryId + " -> " + parentId);
    }
}
```

#### 2.4 Repository 인터페이스 (포트) 구현
```java
public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(CategoryId id);
    List<Category> findAll();
    List<Category> findByParentId(CategoryId parentId);
    List<Category> findRoots();
    boolean existsById(CategoryId id);
    void deleteById(CategoryId id);
    boolean hasChildren(CategoryId categoryId);
}
```

### Step 3: 애플리케이션 레이어 구현
**목표**: 유스케이스와 애플리케이션 서비스 구현

#### 3.1 포트 인터페이스 정의
```java
// Input Port
public interface CategoryUseCase {
    CategoryResponse createCategory(CreateCategoryCommand command);
    CategoryResponse updateCategory(UpdateCategoryCommand command);
    void deleteCategory(CategoryId categoryId);
    CategoryResponse getCategory(CategoryId categoryId);
    CategoryTreeResponse getAllCategories();
    CategoryTreeResponse getCategoryTree(CategoryId rootCategoryId);
}

// Output Port (Repository는 이미 도메인에 정의됨)
```

#### 3.2 Command 객체 구현
```java
public record CreateCategoryCommand(
    @NotBlank String name,
    String description,
    Long parentId
) {}

public record UpdateCategoryCommand(
    @NotNull Long id,
    @NotBlank String name,
    String description,
    Long parentId
) {}
```

#### 3.3 Response 객체 구현
```java
public record CategoryResponse(
    Long id,
    String name,
    String description,
    Long parentId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

public record CategoryTreeResponse(
    List<CategoryNodeResponse> categories
) {}

public record CategoryNodeResponse(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<CategoryNodeResponse> children
) {}
```

#### 3.4 애플리케이션 서비스 구현
```java
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService implements CategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CreateCategoryCommand command) {
        // 1. 부모 카테고리 존재 검증
        if (command.parentId() != null) {
            validateParentExists(new CategoryId(command.parentId()));
        }

        // 2. 카테고리 생성
        Category category = Category.builder()
            .id(new CategoryId(generateNewId()))
            .name(command.name())
            .description(command.description())
            .parentId(command.parentId() != null ? new CategoryId(command.parentId()) : null)
            .build();

        // 3. 저장
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Cacheable(value = "categories", key = "#categoryId.value")
    public CategoryResponse getCategory(CategoryId categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        return categoryMapper.toResponse(category);
    }

    @Override
    @Cacheable(value = "categoryTree", key = "'all'")
    public CategoryTreeResponse getAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        return buildCategoryTree(allCategories, null);
    }

    @Override
    @CacheEvict(value = {"categories", "categoryTree"}, allEntries = true)
    public void deleteCategory(CategoryId categoryId) {
        // 1. 카테고리 존재 검증
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException(categoryId);
        }

        // 2. 하위 카테고리 존재 검증
        if (categoryRepository.hasChildren(categoryId)) {
            throw new CategoryHasChildrenException(categoryId);
        }

        // 3. 삭제
        categoryRepository.deleteById(categoryId);
    }

    private CategoryTreeResponse buildCategoryTree(List<Category> categories, CategoryId parentId) {
        Map<CategoryId, List<Category>> categoryMap = categories.stream()
            .collect(Collectors.groupingBy(
                category -> category.getParentId() != null ? category.getParentId() : null
            ));

        List<CategoryNodeResponse> roots = categoryMap.getOrDefault(parentId, Collections.emptyList())
            .stream()
            .map(category -> buildCategoryNode(category, categoryMap))
            .collect(Collectors.toList());

        return new CategoryTreeResponse(roots);
    }

    private CategoryNodeResponse buildCategoryNode(Category category, Map<CategoryId, List<Category>> categoryMap) {
        List<CategoryNodeResponse> children = categoryMap.getOrDefault(category.getId(), Collections.emptyList())
            .stream()
            .map(child -> buildCategoryNode(child, categoryMap))
            .collect(Collectors.toList());

        return new CategoryNodeResponse(
            category.getId().getValue(),
            category.getName(),
            category.getDescription(),
            category.getCreatedAt(),
            category.getUpdatedAt(),
            children
        );
    }
}
```

### Step 4: 인프라스트럭처 레이어 구현
**목표**: 외부 시스템과의 연동 구현 (DB, Web)

#### 4.1 JPA Repository 구현 (어댑터)
```java
public interface CategoryJpaRepository extends JpaRepository<Category, CategoryId> {
    List<Category> findByParentId(CategoryId parentId);

    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL")
    List<Category> findRoots();

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.parentId = :categoryId")
    boolean hasChildren(@Param("categoryId") CategoryId categoryId);
}

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    @Override
    public Category save(Category category) {
        return jpaRepository.save(category);
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Category> findByParentId(CategoryId parentId) {
        return jpaRepository.findByParentId(parentId);
    }

    @Override
    public List<Category> findRoots() {
        return jpaRepository.findRoots();
    }

    @Override
    public boolean existsById(CategoryId id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(CategoryId id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean hasChildren(CategoryId categoryId) {
        return jpaRepository.hasChildren(categoryId);
    }
}
```

#### 4.2 REST Controller 구현 (어댑터)
```java
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryCommand command) {
        return categoryUseCase.createCategory(command);
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategory(@PathVariable Long id) {
        return categoryUseCase.getCategory(new CategoryId(id));
    }

    @GetMapping
    public CategoryTreeResponse getAllCategories() {
        return categoryUseCase.getAllCategories();
    }

    @GetMapping("/{id}/tree")
    public CategoryTreeResponse getCategoryTree(@PathVariable Long id) {
        return categoryUseCase.getCategoryTree(new CategoryId(id));
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(
        @PathVariable Long id,
        @Valid @RequestBody UpdateCategoryRequest request) {

        UpdateCategoryCommand command = new UpdateCategoryCommand(
            id, request.name(), request.description(), request.parentId());

        return categoryUseCase.updateCategory(command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryUseCase.deleteCategory(new CategoryId(id));
    }
}

public record CreateCategoryRequest(
    @NotBlank String name,
    String description,
    Long parentId
) {}

public record UpdateCategoryRequest(
    @NotBlank String name,
    String description,
    Long parentId
) {}
```

#### 4.3 글로벌 예외 처리
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCategoryNotFound(CategoryNotFoundException ex) {
        return new ErrorResponse("CATEGORY_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(CategoryHasChildrenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCategoryHasChildren(CategoryHasChildrenException ex) {
        return new ErrorResponse("CATEGORY_HAS_CHILDREN", ex.getMessage());
    }

    @ExceptionHandler(CircularReferenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCircularReference(CircularReferenceException ex) {
        return new ErrorResponse("CIRCULAR_REFERENCE", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));

        return new ErrorResponse("VALIDATION_FAILED", message);
    }
}

public record ErrorResponse(String code, String message) {}
```

### Step 5: 설정 및 매퍼 구현
**목표**: 설정 클래스와 매퍼 구현

#### 5.1 매퍼 구현
```java
@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId().getValue(),
            category.getName(),
            category.getDescription(),
            category.getParentId() != null ? category.getParentId().getValue() : null,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }

    public Category toEntity(CreateCategoryCommand command) {
        return Category.builder()
            .name(command.name())
            .description(command.description())
            .parentId(command.parentId() != null ? new CategoryId(command.parentId()) : null)
            .build();
    }
}
```

#### 5.2 캐시 설정
```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(Duration.ofMinutes(5)));
        return cacheManager;
    }
}
```

### Step 6: 테스트 구현
**목표**: 단위 테스트 및 통합 테스트 구현

#### 6.1 도메인 테스트
```java
class CategoryTest {

    @Test
    void 카테고리_생성_테스트() {
        // given
        String name = "전자제품";
        String description = "전자제품 카테고리";

        // when
        Category category = Category.builder()
            .id(new CategoryId(1L))
            .name(name)
            .description(description)
            .build();

        // then
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getDescription()).isEqualTo(description);
        assertThat(category.isRoot()).isTrue();
    }

    @Test
    void 빈_이름으로_카테고리_생성시_예외발생() {
        // given & when & then
        assertThatThrownBy(() ->
            Category.builder()
                .id(new CategoryId(1L))
                .name("")
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
```

#### 6.2 서비스 테스트
```java
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void 카테고리_생성_성공() {
        // given
        CreateCategoryCommand command = new CreateCategoryCommand("전자제품", "전자제품 카테고리", null);
        Category savedCategory = createCategory(1L, "전자제품", "전자제품 카테고리", null);
        CategoryResponse expectedResponse = new CategoryResponse(1L, "전자제품", "전자제품 카테고리", null, null, null);

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(expectedResponse);

        // when
        CategoryResponse result = categoryService.createCategory(command);

        // then
        assertThat(result.name()).isEqualTo("전자제품");
        assertThat(result.description()).isEqualTo("전자제품 카테고리");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void 존재하지_않는_카테고리_조회시_예외발생() {
        // given
        CategoryId categoryId = new CategoryId(999L);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategory(categoryId))
            .isInstanceOf(CategoryNotFoundException.class);
    }
}
```

#### 6.3 통합 테스트
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class CategoryControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void 카테고리_전체_조회_통합테스트() {
        // given
        setupTestData();

        // when
        ResponseEntity<CategoryTreeResponse> response =
            restTemplate.getForEntity("/api/v1/categories", CategoryTreeResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().categories()).hasSize(2); // 루트 카테고리 2개
    }

    private void setupTestData() {
        // 테스트 데이터 설정
        Category electronics = Category.builder()
            .id(new CategoryId(1L))
            .name("전자제품")
            .build();

        Category smartphone = Category.builder()
            .id(new CategoryId(2L))
            .name("스마트폰")
            .parentId(new CategoryId(1L))
            .build();

        categoryRepository.save(electronics);
        categoryRepository.save(smartphone);
    }
}
```

### Step 7: API 문서화 및 운영 설정
**목표**: API 문서화 및 운영을 위한 설정

#### 7.1 OpenAPI 설정
```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI categoryServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Category Service API")
                .description("온라인 쇼핑몰 카테고리 관리 서비스")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Development Team")
                    .email("dev@example.com")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local Development Server")
            ));
    }
}
```

#### 7.2 Actuator 설정 (application.yml에 추가)
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics
  endpoint:
    health:
      show-details: always
```

## 4. 구현 순서 요약

1. **Step 1**: 프로젝트 초기 설정 (build.gradle, application.yml)
2. **Step 2**: 도메인 레이어 (엔티티, Value Object, 예외, Repository 인터페이스)
3. **Step 3**: 애플리케이션 레이어 (유스케이스, 서비스, Command/Response 객체)
4. **Step 4**: 인프라스트럭처 레이어 (JPA Repository, REST Controller)
5. **Step 5**: 설정 및 매퍼 (캐시 설정, 매퍼)
6. **Step 6**: 테스트 (단위 테스트, 통합 테스트)
7. **Step 7**: 문서화 및 운영 설정

각 단계는 독립적으로 구현하고 테스트할 수 있도록 설계되어 있으며, Hexagonal Architecture의 원칙을 따라 각 레이어 간의 의존성이 올바른 방향으로 흐르도록 구성되어 있습니다.