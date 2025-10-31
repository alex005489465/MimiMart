package com.mimimart.api.controller.admin;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.category.CategoryResponse;
import com.mimimart.api.dto.category.CreateCategoryRequest;
import com.mimimart.api.dto.category.UpdateCategoryRequest;
import com.mimimart.application.service.CategoryService;
import com.mimimart.infrastructure.persistence.entity.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 後台分類管理 Controller
 */
@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
@Tag(name = "後台 - 商品分類管理", description = "後台商品分類管理 API")
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * 查詢所有分類列表
     */
    @GetMapping("/list")
    @Operation(summary = "查詢分類列表", description = "查詢所有分類 (包含已刪除)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryList() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryResponse> response = categories.stream()
            .map(CategoryResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response));
    }

    /**
     * 查詢分類詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查詢分類詳情", description = "根據 ID 查詢分類詳細資訊")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryDetail(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        CategoryResponse response = CategoryResponse.from(category);

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response));
    }

    /**
     * 新增分類
     */
    @PostMapping("/create")
    @Operation(summary = "新增分類", description = "建立新的商品分類")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {

        Category category = categoryService.createCategory(
            request.getName(),
            request.getDescription(),
            request.getSortOrder()
        );

        CategoryResponse response = CategoryResponse.from(category);
        return ResponseEntity.ok(ApiResponse.success("新增成功", response));
    }

    /**
     * 更新分類
     */
    @PostMapping("/{id}/update")
    @Operation(summary = "更新分類", description = "更新分類資訊")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        Category category = categoryService.updateCategory(
            id,
            request.getName(),
            request.getDescription(),
            request.getSortOrder()
        );

        CategoryResponse response = CategoryResponse.from(category);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    /**
     * 刪除分類
     */
    @PostMapping("/{id}/delete")
    @Operation(summary = "刪除分類", description = "軟刪除分類")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("刪除成功"));
    }
}
