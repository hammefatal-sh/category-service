package com.example.categoryservice.domain.exception;

import com.example.categoryservice.domain.model.CategoryId;

public class CategoryHasChildrenException extends RuntimeException {

    public CategoryHasChildrenException(CategoryId categoryId) {
        super("Cannot delete category with children: " + categoryId.getValue());
    }

    public CategoryHasChildrenException(String message) {
        super(message);
    }
}