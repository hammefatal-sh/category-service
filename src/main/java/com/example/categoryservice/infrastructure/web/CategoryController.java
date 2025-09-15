package com.example.categoryservice.infrastructure.web;

import com.example.categoryservice.application.port.in.CategoryUseCase;
import com.example.categoryservice.application.port.in.CreateCategoryCommand;
import com.example.categoryservice.application.port.in.UpdateCategoryCommand;
import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.model.CategoryId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CreateCategoryCommand command = new CreateCategoryCommand(
            request.name(),
            request.description(),
            request.parentId()
        );
        return categoryUseCase.createCategory(command);
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategory(@PathVariable Long id) {
        return categoryUseCase.getCategory(new CategoryId(id));
    }

    @GetMapping
    public CategoryTreeResponse getAllCategories() {
        return categoryUseCase.getAllCategories();
    }

    @GetMapping("/{id}/tree")
    public CategoryTreeResponse getCategoryTree(@PathVariable Long id) {
        return categoryUseCase.getCategoryTree(new CategoryId(id));
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(
        @PathVariable Long id,
        @Valid @RequestBody UpdateCategoryRequest request) {

        UpdateCategoryCommand command = new UpdateCategoryCommand(
            id,
            request.name(),
            request.description(),
            request.parentId()
        );

        return categoryUseCase.updateCategory(command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryUseCase.deleteCategory(new CategoryId(id));
    }
}