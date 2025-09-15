package com.example.categoryservice.infrastructure.web;

import com.example.categoryservice.application.port.in.CategoryUseCase;
import com.example.categoryservice.application.port.in.CreateCategoryCommand;
import com.example.categoryservice.application.port.in.UpdateCategoryCommand;
import com.example.categoryservice.application.port.out.CategoryResponse;
import com.example.categoryservice.application.port.out.CategoryTreeResponse;
import com.example.categoryservice.domain.model.CategoryId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Categories", description = "카테고리 관리 API")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    @Operation(
            summary = "카테고리 생성",
            description = "새로운 카테고리를 생성합니다. 루트 카테고리 또는 특정 부모 카테고리의 하위 카테고리로 생성할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "카테고리가 성공적으로 생성됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponse.class),
                            examples = @ExampleObject(
                                    name = "루트 카테고리 생성 성공",
                                    value = """
                                            {
                                              "id": 1,
                                              "name": "전자제품",
                                              "description": "전자제품 카테고리",
                                              "parent_id": null,
                                              "created_at": "2025-01-01T10:00:00",
                                              "updated_at": "2025-01-01T10:00:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (validation 실패, 부모 카테고리 없음 등)",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "생성할 카테고리 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateCategoryRequest.class),
                            examples = @ExampleObject(
                                    name = "루트 카테고리 생성",
                                    value = """
                                            {
                                              "name": "전자제품",
                                              "description": "전자제품 카테고리",
                                              "parent_id": null
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody CreateCategoryRequest request) {
        CreateCategoryCommand command = new CreateCategoryCommand(
            request.name(),
            request.description(),
            request.parentId()
        );
        return categoryUseCase.createCategory(command);
    }

    @Operation(
            summary = "카테고리 단일 조회",
            description = "ID로 특정 카테고리의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "카테고리 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public CategoryResponse getCategory(
            @Parameter(description = "조회할 카테고리 ID", required = true, example = "1")
            @Positive @PathVariable Long id) {
        return categoryUseCase.getCategory(new CategoryId(id));
    }

    @Operation(
            summary = "전체 카테고리 트리 조회",
            description = "모든 카테고리를 계층적 트리 구조로 조회합니다. 루트 카테고리부터 모든 하위 카테고리까지 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 카테고리 트리 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryTreeResponse.class),
                            examples = @ExampleObject(
                                    name = "카테고리 트리 예시",
                                    value = """
                                            {
                                              "categories": [
                                                {
                                                  "id": 1,
                                                  "name": "전자제품",
                                                  "description": "전자제품 카테고리",
                                                  "created_at": "2025-01-01T10:00:00",
                                                  "updated_at": "2025-01-01T10:00:00",
                                                  "children": [
                                                    {
                                                      "id": 2,
                                                      "name": "스마트폰",
                                                      "description": "스마트폰 카테고리",
                                                      "created_at": "2025-01-01T10:00:00",
                                                      "updated_at": "2025-01-01T10:00:00",
                                                      "children": []
                                                    }
                                                  ]
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public CategoryTreeResponse getAllCategories() {
        return categoryUseCase.getAllCategories();
    }

    @Operation(
            summary = "특정 카테고리 하위 트리 조회",
            description = "지정된 카테고리를 루트로 하는 하위 트리 구조를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "카테고리 트리 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryTreeResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "루트 카테고리를 찾을 수 없음")
    })
    @GetMapping("/{id}/tree")
    public CategoryTreeResponse getCategoryTree(
            @Parameter(description = "트리 루트로 사용할 카테고리 ID", required = true, example = "1")
            @Positive @PathVariable Long id) {
        return categoryUseCase.getCategoryTree(new CategoryId(id));
    }

    @Operation(
            summary = "카테고리 수정",
            description = "기존 카테고리의 정보를 수정합니다. 이름, 설명, 부모 카테고리를 변경할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "카테고리 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 순환 참조 등)"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public CategoryResponse updateCategory(
            @Parameter(description = "수정할 카테고리 ID", required = true, example = "1")
            @Positive @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 카테고리 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateCategoryRequest.class),
                            examples = @ExampleObject(
                                    name = "카테고리 수정",
                                    value = """
                                            {
                                              "name": "전자기기",
                                              "description": "전자기기 카테고리",
                                              "parent_id": null
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody UpdateCategoryRequest request) {

        UpdateCategoryCommand command = new UpdateCategoryCommand(
            id,
            request.name(),
            request.description(),
            request.parentId()
        );

        return categoryUseCase.updateCategory(command);
    }

    @Operation(
            summary = "카테고리 삭제",
            description = "지정된 카테고리를 삭제합니다. 하위 카테고리가 있는 경우 삭제할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "하위 카테고리가 존재하여 삭제할 수 없음"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @Parameter(description = "삭제할 카테고리 ID", required = true, example = "1")
            @Positive @PathVariable Long id) {
        categoryUseCase.deleteCategory(new CategoryId(id));
    }
}