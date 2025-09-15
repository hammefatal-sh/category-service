package com.example.categoryservice.domain.exception;

import com.example.categoryservice.domain.model.CategoryId;

public class CircularReferenceException extends RuntimeException {

    public CircularReferenceException(CategoryId categoryId, CategoryId parentId) {
        super("Circular reference detected: " + categoryId.getValue() + " -> " + parentId.getValue());
    }

    public CircularReferenceException(String message) {
        super(message);
    }
}