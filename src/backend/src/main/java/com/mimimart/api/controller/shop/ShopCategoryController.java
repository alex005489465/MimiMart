package com.mimimart.api.controller.shop;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.category.CategoryResponse;
import com.mimimart.application.service.CategoryService;
import com.mimimart.infrastructure.persistence.entity.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台分類 Controller
 */
@RestController
@RequestMapping("/api/shop/category")
@RequiredArgsConstructor
@Tag(name = "前台 - 商品分類", description = "前台商品分類查詢 API")
public class ShopCategoryController {

    private final CategoryService categoryService;

    /**
     * 查詢分類列表
     */
    @GetMapping("/list")
    @Operation(summary = "查詢分類列表", description = "取得所有可用的商品分類 (依排序權重排序)")
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
}
