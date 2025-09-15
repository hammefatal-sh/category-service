package com.example.categoryservice.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(max = 100, message = "카테고리 이름은 100자를 초과할 수 없습니다")
    String name,

    @Size(max = 500, message = "카테고리 설명은 500자를 초과할 수 없습니다")
    String description,

    Long parentId
) {
}