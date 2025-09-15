package com.example.categoryservice.domain.exception;

import com.example.categoryservice.domain.model.CategoryId;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(CategoryId categoryId) {
        super("Category not found: " + categoryId.getValue());
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }
}