package com.example.categoryservice.infrastructure.web;

import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.model.Category;
import com.example.categoryservice.domain.model.CategoryId;
import com.example.categoryservice.domain.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@ActiveProfiles("test")
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
    }

    @Test
    void 카테고리_생성_성공() throws Exception {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest("전자제품", "전자제품 카테고리", null);

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("전자제품"))
            .andExpect(jsonPath("$.description").value("전자제품 카테고리"))
            .andExpect(jsonPath("$.parentId").isEmpty());
    }

    @Test
    void 하위_카테고리_생성_성공() throws Exception {
        // given
        Category parentCategory = Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리");
        categoryRepository.save(parentCategory);

        CreateCategoryRequest request = new CreateCategoryRequest("스마트폰", "스마트폰 카테고리", 1L);

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("스마트폰"))
            .andExpect(jsonPath("$.parentId").value(1L));
    }

    @Test
    void 카테고리_생성시_validation_실패() throws Exception {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest("", "설명", null);

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void 카테고리_조회_성공() throws Exception {
        // given
        Category category = Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리");
        categoryRepository.save(category);

        // when & then
        mockMvc.perform(get("/api/v1/categories/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("전자제품"));
    }

    @Test
    void 존재하지_않는_카테고리_조회시_404() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/categories/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    void 카테고리_수정_성공() throws Exception {
        // given
        Category category = Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리");
        categoryRepository.save(category);

        UpdateCategoryRequest request = new UpdateCategoryRequest("전자기기", "전자기기 카테고리", null);

        // when & then
        mockMvc.perform(put("/api/v1/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("전자기기"))
            .andExpect(jsonPath("$.description").value("전자기기 카테고리"));
    }

    @Test
    void 카테고리_삭제_성공() throws Exception {
        // given
        Category category = Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리");
        categoryRepository.save(category);

        // when & then
        mockMvc.perform(delete("/api/v1/categories/1"))
            .andExpect(status().isNoContent());

        // verify
        assertThat(categoryRepository.existsById(new CategoryId(1L))).isFalse();
    }

    @Test
    void 하위_카테고리가_있는_카테고리_삭제시_400() throws Exception {
        // given
        Category parent = Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리");
        Category child = Category.create(new CategoryId(2L), "스마트폰", "스마트폰 카테고리", new CategoryId(1L));
        categoryRepository.save(parent);
        categoryRepository.save(child);

        // when & then
        mockMvc.perform(delete("/api/v1/categories/1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CATEGORY_HAS_CHILDREN"));
    }

    @Test
    void 전체_카테고리_트리_조회() throws Exception {
        // given
        Category electronics = Category.createRoot(new CategoryId(1L), "전자제품", "전자제품 카테고리");
        Category smartphone = Category.create(new CategoryId(2L), "스마트폰", "스마트폰 카테고리", new CategoryId(1L));
        categoryRepository.save(electronics);
        categoryRepository.save(smartphone);

        // when & then
        mockMvc.perform(get("/api/v1/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categories").isArray())
            .andExpect(jsonPath("$.categories[0].name").value("전자제품"))
            .andExpect(jsonPath("$.categories[0].children").isArray())
            .andExpect(jsonPath("$.categories[0].children[0].name").value("스마트폰"));
    }
}