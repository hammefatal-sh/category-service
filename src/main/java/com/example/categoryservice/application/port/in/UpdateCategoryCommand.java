package com.example.categoryservice.application.port.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCategoryCommand(
    @NotNull(message = "카테고리 ID는 필수입니다")
    Long id,

    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(max = 100, message = "카테고리 이름은 100자를 초과할 수 없습니다")
    String name,

    @Size(max = 500, message = "카테고리 설명은 500자를 초과할 수 없습니다")
    String description,

    Long parentId
) {
}