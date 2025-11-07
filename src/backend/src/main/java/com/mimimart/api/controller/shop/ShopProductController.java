package com.mimimart.api.controller.shop;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.product.ProductDetailResponse;
import com.mimimart.api.dto.product.ProductResponse;
import com.mimimart.application.service.ProductService;
import com.mimimart.infrastructure.persistence.entity.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 前台商品 Controller
 */
@RestController
@RequestMapping("/api/shop/product")
@RequiredArgsConstructor
@Tag(name = "前台 - 商品", description = "前台商品查詢 API")
public class ShopProductController {

    private final ProductService productService;

    /**
     * 查詢商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "查詢商品列表", description = "查詢已上架的商品列表,支援分頁和排序")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductList(
            @Parameter(description = "分類 ID (可選)") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "頁碼 (從 1 開始)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序欄位 (createdAt, price, name)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向 (asc, desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        // 將前端的 1-based 頁碼轉換為 Spring Data JPA 的 0-based
        int zeroBasedPage = page - 1;
        // 建立排序
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(zeroBasedPage, size, sort);

        // 查詢商品
        Page<Product> productPage;
        if (categoryId != null) {
            productPage = productService.getPublishedProductsByCategory(categoryId, pageable);
        } else {
            productPage = productService.getPublishedProducts(pageable);
        }

        // 轉換回應
        List<ProductResponse> response = productPage.getContent().stream()
            .map(ProductResponse::from)
            .collect(Collectors.toList());

        // 建立分頁資訊
        Map<String, Object> meta = new HashMap<>();
        // 將 0-based 頁碼轉換為 1-based 返回給前端
        meta.put("currentPage", productPage.getNumber() + 1);
        meta.put("totalPages", productPage.getTotalPages());
        meta.put("totalItems", productPage.getTotalElements());
        meta.put("pageSize", productPage.getSize());

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response, meta));
    }

    /**
     * 查詢商品詳情
     */
    @GetMapping("/detail")
    @Operation(summary = "查詢商品詳情", description = "根據 ID 查詢商品詳細資訊")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(
            @Parameter(description = "商品 ID") @RequestParam Long productId) {
        Product product = productService.getPublishedProductById(productId);
        ProductDetailResponse response = ProductDetailResponse.from(product);

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response));
    }

    /**
     * 搜尋商品
     */
    @GetMapping("/search")
    @Operation(summary = "搜尋商品", description = "根據關鍵字搜尋商品名稱")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @Parameter(description = "搜尋關鍵字") @RequestParam String keyword,
            @Parameter(description = "頁碼 (從 1 開始)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "20") int size) {

        // 將前端的 1-based 頁碼轉換為 Spring Data JPA 的 0-based
        int zeroBasedPage = page - 1;
        Pageable pageable = PageRequest.of(zeroBasedPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage = productService.searchProducts(keyword, pageable);

        List<ProductResponse> response = productPage.getContent().stream()
            .map(ProductResponse::from)
            .collect(Collectors.toList());

        Map<String, Object> meta = new HashMap<>();
        // 將 0-based 頁碼轉換為 1-based 返回給前端
        meta.put("currentPage", productPage.getNumber() + 1);
        meta.put("totalPages", productPage.getTotalPages());
        meta.put("totalItems", productPage.getTotalElements());
        meta.put("pageSize", productPage.getSize());

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response, meta));
    }
}
