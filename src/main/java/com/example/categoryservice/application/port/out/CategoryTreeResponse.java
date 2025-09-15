package com.example.categoryservice.application.port.out;

import java.util.List;

public record CategoryTreeResponse(
    List<CategoryNodeResponse> categories
) {
}