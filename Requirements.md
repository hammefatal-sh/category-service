# Requirements

## 1. 과제 개요

### 1.1 서비스 개요
- **서비스명**: Category Service
- **목적**: 온라인 쇼핑몰의 상품 카테고리 관리를 위한 마이크로서비스
- **아키텍처**: Hexagonal Architecture (Ports & Adapters Pattern)
- **개발 언어**: Java 17
- **프레임워크**: Spring Boot 3.2.0

### 1.2 기술 스택
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: H2 Database (Embedded)
- **Build Tool**: Gradle
- **Architecture**: Hexagonal Architecture (Ports & Adapters)
- **API Documentation**: SpringDoc OpenAPI 3.0
- **Cache**: Caffeine
- **Validation**: Jakarta Validation

## 2. 기능

### 2.1 필수 기능
- **카테고리 등록/수정/삭제 API**
- **카테고리 조회 API**
  - 카테고리 조회 시 자기 자신을 포함해 하위 카테고리 조회가 가능해야 한다.
  - 카테고리를 지정하지 않을 시, 전체 카테고리를 반환한다.
  - 카테고리는 트리 구조로 반환한다.