package com.example.categoryservice.infrastructure.web;

import com.example.categoryservice.domain.exception.CategoryHasChildrenException;
import com.example.categoryservice.domain.exception.CategoryNotFoundException;
import com.example.categoryservice.domain.exception.CircularReferenceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCategoryNotFound(CategoryNotFoundException ex) {
        log.warn("Category not found: {}", ex.getMessage());
        return new ErrorResponse("CATEGORY_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(CategoryHasChildrenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCategoryHasChildren(CategoryHasChildrenException ex) {
        log.warn("Category has children: {}", ex.getMessage());
        return new ErrorResponse("CATEGORY_HAS_CHILDREN", ex.getMessage());
    }

    @ExceptionHandler(CircularReferenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCircularReference(CircularReferenceException ex) {
        log.warn("Circular reference detected: {}", ex.getMessage());
        return new ErrorResponse("CIRCULAR_REFERENCE", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", message);
        return new ErrorResponse("VALIDATION_FAILED", message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return new ErrorResponse("INVALID_ARGUMENT", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred");
    }
}