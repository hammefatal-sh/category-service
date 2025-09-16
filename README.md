# category-service

## Tools
- Claude Pro (Claude Code)
- VSCode

## 가이드라인
- Requirements.md

## 상세설계
- DetailedDesign.md 

## Prompt
  1. Requirements.md 파일의 요구사항을 분석하고, DetailedDesign.md 파일을 생성하여 파일에 상세 설계 사항을 작성해줘. 
설계 내용은 Step by Step 으로 구현할 수 있도록 작성해줘.
  2. DetailedDesign.md 문서의 Step 1 부분까지 구현하고, SpringBoot 어플리케이션이 정상 동작하는지 확인해줘.
  3. Step 2 구현하고, 테스트까지 해줘.
  4. Step 3 구현하고, 테스트까지 해줘.
  5. Step 4 구현하고, 테스트까지 해줘.
  6. Step 5 구현하고, 테스트까지 해줘.
  7. Step 6 구현하고, 테스트까지 해줘.
  8. Step 7 구현하고, 테스트까지 해줘.


## 테이블 명세
```sql
CREATE TABLE categories (
    id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    parent_id BIGINT,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);
```

## 컬럼 명세
| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
|--------|-------------|-----------|------|
| `id` | `BIGINT` | `NOT NULL`, `PRIMARY KEY` | 카테고리 고유 식별자 |
| `name` | `VARCHAR(100)` | `NOT NULL` | 카테고리 이름 (최대 100자) |
| `description` | `VARCHAR(500)` | `NULL` | 카테고리 설명 (최대 500자) |
| `parent_id` | `BIGINT` | `NULL`, `FOREIGN KEY` | 부모 카테고리 ID (셀프 참조) |
| `created_at` | `TIMESTAMP(6)` | `NOT NULL` | 생성 일시 (자동 생성) |
| `updated_at` | `TIMESTAMP(6)` | `NOT NULL` | 수정 일시 (자동 갱신) |


## API 명세
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/api-docs


## 어플리케이션 실행
  1. git clone https://github.com/hammefatal-sh/category-service.git
     cd category-service
  2. gradle wrapper
  3. ./gradlew clean build
  4. ./gradlew bootRun


## 테스트
  1. 전체 테스트 실행
    ./gradlew test
  2. 특정 테스트 실행
    ./gradlew test --tests CategoryServiceTest
  3. 테스트 리포트 확인
    open build/reports/tests/test/index.html