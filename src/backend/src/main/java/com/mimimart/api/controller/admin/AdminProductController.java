package com.mimimart.api.controller.admin;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.product.CreateProductRequest;
import com.mimimart.api.dto.product.ProductDetailResponse;
import com.mimimart.api.dto.product.ProductIdRequest;
import com.mimimart.api.dto.product.ProductResponse;
import com.mimimart.api.dto.product.UpdateProductRequest;
import com.mimimart.application.service.ProductService;
import com.mimimart.infrastructure.persistence.entity.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * 後台商品管理 Controller
 */
@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
@Tag(name = "後台 - 商品管理", description = "後台商品管理 API")
public class AdminProductController {

    private final ProductService productService;

    /**
     * 查詢商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "查詢商品列表", description = "查詢所有商品,支援狀態篩選和分頁")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductList(
            @Parameter(description = "上架狀態 (published, unpublished, all)") @RequestParam(defaultValue = "all") String status,
            @Parameter(description = "頁碼 (從 1 開始)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "20") int size) {

        // 將前端的 1-based 頁碼轉換為 Spring Data JPA 的 0-based
        int zeroBasedPage = page - 1;
        Pageable pageable = PageRequest.of(zeroBasedPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 根據狀態查詢
        Page<Product> productPage;
        switch (status.toLowerCase()) {
            case "published":
                productPage = productService.getProductsByPublishStatus(true, pageable);
                break;
            case "unpublished":
                productPage = productService.getProductsByPublishStatus(false, pageable);
                break;
            default:
                productPage = productService.getAllProducts(pageable);
                break;
        }

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

    /**
     * 查詢商品詳情
     */
    @GetMapping("/detail")
    @Operation(summary = "查詢商品詳情", description = "根據 ID 查詢商品詳細資訊")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(
            @Parameter(description = "商品 ID") @RequestParam Long productId) {
        Product product = productService.getProductById(productId);
        ProductDetailResponse response = ProductDetailResponse.from(product);

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response));
    }

    /**
     * 新增商品
     */
    @PostMapping("/create")
    @Operation(summary = "新增商品", description = "建立新的商品")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        Product product = productService.createProduct(
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getImageUrl(),
            request.getCategoryId(),
            request.getPublishedAt(),
            request.getUnpublishedAt()
        );

        ProductDetailResponse response = ProductDetailResponse.from(product);
        return ResponseEntity.ok(ApiResponse.success("新增成功", response));
    }

    /**
     * 更新商品
     */
    @PostMapping("/update")
    @Operation(summary = "更新商品", description = "更新商品資訊")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @Valid @RequestBody UpdateProductRequest request) {

        Product product = productService.updateProduct(
            request.getProductId(),
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getImageUrl(),
            request.getCategoryId(),
            request.getPublishedAt(),
            request.getUnpublishedAt()
        );

        ProductDetailResponse response = ProductDetailResponse.from(product);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    /**
     * 刪除商品
     */
    @PostMapping("/delete")
    @Operation(summary = "刪除商品", description = "軟刪除商品")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@Valid @RequestBody ProductIdRequest request) {
        productService.deleteProduct(request.getProductId());
        return ResponseEntity.ok(ApiResponse.success("刪除成功"));
    }

    /**
     * 上架商品
     */
    @PostMapping("/publish")
    @Operation(summary = "上架商品", description = "將商品設為上架狀態")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> publishProduct(@Valid @RequestBody ProductIdRequest request) {
        Product product = productService.publishProduct(request.getProductId());
        ProductDetailResponse response = ProductDetailResponse.from(product);

        return ResponseEntity.ok(ApiResponse.success("上架成功", response));
    }

    /**
     * 下架商品
     */
    @PostMapping("/unpublish")
    @Operation(summary = "下架商品", description = "將商品設為下架狀態")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> unpublishProduct(@Valid @RequestBody ProductIdRequest request) {
        Product product = productService.unpublishProduct(request.getProductId());
        ProductDetailResponse response = ProductDetailResponse.from(product);

        return ResponseEntity.ok(ApiResponse.success("下架成功", response));
    }

    /**
     * 啟用商品
     */
    @PostMapping("/activate")
    @Operation(summary = "啟用商品", description = "將商品設為啟用狀態")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> activateProduct(@Valid @RequestBody ProductIdRequest request) {
        Product product = productService.activateProduct(request.getProductId());
        ProductDetailResponse response = ProductDetailResponse.from(product);

        return ResponseEntity.ok(ApiResponse.success("啟用成功", response));
    }

    /**
     * 停用商品
     */
    @PostMapping("/deactivate")
    @Operation(summary = "停用商品", description = "將商品設為停用狀態（自動下架）")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> deactivateProduct(@Valid @RequestBody ProductIdRequest request) {
        Product product = productService.deactivateProduct(request.getProductId());
        ProductDetailResponse response = ProductDetailResponse.from(product);

        return ResponseEntity.ok(ApiResponse.success("停用成功", response));
    }
}
