package com.example.categoryservice.infrastructure.web;

import java.time.LocalDateTime;

public record ErrorResponse(
    String code,
    String message,
    LocalDateTime timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, LocalDateTime.now());
    }
}