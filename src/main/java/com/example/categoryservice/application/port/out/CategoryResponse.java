package com.example.categoryservice.application.port.out;

import java.time.LocalDateTime;

public record CategoryResponse(
    Long id,
    String name,
    String description,
    Long parentId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}