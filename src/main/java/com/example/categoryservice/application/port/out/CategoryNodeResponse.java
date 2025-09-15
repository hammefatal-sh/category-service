package com.example.categoryservice.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryNodeResponse(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<CategoryNodeResponse> children
) {
}