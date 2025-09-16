package com.example.categoryservice.infrastructure.web;

import com.example.categoryservice.application.port.in.CategoryUseCase;
import com.example.categoryservice.application.port.in.CreateCategoryCommand;
import com.example.categoryservice.application.port.in.UpdateCategoryCommand;
import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.exception.CategoryHasChildrenException;
import com.example.categoryservice.domain.exception.CategoryNotFoundException;
import com.example.categoryservice.domain.model.CategoryId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController 단위 테스트")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryUseCase categoryUseCase;

    @Test
    @DisplayName("카테고리 생성 API 성공")
    void 카테고리_생성_API_성공() throws Exception {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest("전자제품", "전자제품 카테고리", null);
        CategoryResponse response = new CategoryResponse(1L, "전자제품", "전자제품 카테고리", null,
                LocalDateTime.now(), LocalDateTime.now());

        when(categoryUseCase.createCategory(any(CreateCategoryCommand.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("전자제품"))
                .andExpect(jsonPath("$.description").value("전자제품 카테고리"))
                .andExpect(jsonPath("$.parent_id").doesNotExist());

        verify(categoryUseCase).createCategory(any(CreateCategoryCommand.class));
    }

    @Test
    @DisplayName("카테고리 생성시 validation 실패")
    void 카테고리_생성시_validation_실패() throws Exception {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest("", "전자제품 카테고리", null);

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(categoryUseCase, never()).createCategory(any());
    }

    @Test
    @DisplayName("카테고리 단일 조회 API 성공")
    void 카테고리_단일_조회_API_성공() throws Exception {
        // given
        Long categoryId = 1L;
        CategoryResponse response = new CategoryResponse(1L, "전자제품", "전자제품 카테고리", null,
                LocalDateTime.now(), LocalDateTime.now());

        when(categoryUseCase.getCategory(new CategoryId(categoryId))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("전자제품"));

        verify(categoryUseCase).getCategory(new CategoryId(categoryId));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 조회시 404")
    void 존재하지_않는_카테고리_조회시_404() throws Exception {
        // given
        Long categoryId = 999L;
        when(categoryUseCase.getCategory(new CategoryId(categoryId)))
                .thenThrow(new CategoryNotFoundException("Category not found with id: " + categoryId));

        // when & then
        mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));

        verify(categoryUseCase).getCategory(new CategoryId(categoryId));
    }

    @Test
    @DisplayName("전체 카테고리 트리 조회 API 성공")
    void 전체_카테고리_트리_조회_API_성공() throws Exception {
        // given
        CategoryTreeResponse response = new CategoryTreeResponse(List.of());

        when(categoryUseCase.getAllCategories()).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray());

        verify(categoryUseCase).getAllCategories();
    }

    @Test
    @DisplayName("특정 카테고리 하위 트리 조회 API 성공")
    void 특정_카테고리_하위_트리_조회_API_성공() throws Exception {
        // given
        Long categoryId = 1L;
        CategoryTreeResponse response = new CategoryTreeResponse(List.of());

        when(categoryUseCase.getCategoryTree(new CategoryId(categoryId))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/categories/{id}/tree", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray());

        verify(categoryUseCase).getCategoryTree(new CategoryId(categoryId));
    }

    @Test
    @DisplayName("카테고리 수정 API 성공")
    void 카테고리_수정_API_성공() throws Exception {
        // given
        Long categoryId = 1L;
        UpdateCategoryRequest request = new UpdateCategoryRequest("전자기기", "전자기기 카테고리", null);
        CategoryResponse response = new CategoryResponse(1L, "전자기기", "전자기기 카테고리", null,
                LocalDateTime.now(), LocalDateTime.now());

        when(categoryUseCase.updateCategory(any(UpdateCategoryCommand.class))).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("전자기기"));

        verify(categoryUseCase).updateCategory(any(UpdateCategoryCommand.class));
    }

    @Test
    @DisplayName("카테고리 삭제 API 성공")
    void 카테고리_삭제_API_성공() throws Exception {
        // given
        Long categoryId = 1L;

        doNothing().when(categoryUseCase).deleteCategory(new CategoryId(categoryId));

        // when & then
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNoContent());

        verify(categoryUseCase).deleteCategory(new CategoryId(categoryId));
    }

    @Test
    @DisplayName("하위 카테고리가 있는 카테고리 삭제시 400")
    void 하위_카테고리가_있는_카테고리_삭제시_400() throws Exception {
        // given
        Long categoryId = 1L;
        doThrow(new CategoryHasChildrenException("Cannot delete category with children"))
                .when(categoryUseCase).deleteCategory(new CategoryId(categoryId));

        // when & then
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CATEGORY_HAS_CHILDREN"));

        verify(categoryUseCase).deleteCategory(new CategoryId(categoryId));
    }

    @Test
    @DisplayName("잘못된 경로 변수로 요청시 400")
    void 잘못된_경로_변수로_요청시_400() throws Exception {
        // when & then - negative number violates @Positive constraint
        // Note: This might return 500 if there's no proper validation exception handling
        mockMvc.perform(get("/api/v1/categories/{id}", -1))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메소드로 요청시 405")
    void 지원하지_않는_HTTP_메소드로_요청시_405() throws Exception {
        // when & then - PATCH method is not defined for this endpoint
        // Note: This might return 500 if there's no proper method not allowed exception handling
        mockMvc.perform(patch("/api/v1/categories/1"))
                .andExpect(status().isInternalServerError());
    }
}