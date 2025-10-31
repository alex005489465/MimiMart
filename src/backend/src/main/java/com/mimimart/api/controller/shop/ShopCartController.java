package com.mimimart.api.controller.shop;

import com.mimimart.api.dto.cart.*;
import com.mimimart.api.dto.ApiResponse;
import com.mimimart.infrastructure.security.CustomUserDetails;
import com.mimimart.application.service.CartService;
import com.mimimart.shared.valueobject.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 前台購物車控制器
 * 處理消費者購物車操作的 API 端點
 *
 * @author MimiMart Development Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/shop/cart")
public class ShopCartController {

    @Autowired
    private CartService cartService;

    /**
     * 驗證使用者是否為會員
     */
    private void validateMemberAccess(CustomUserDetails userDetails) {
        if (userDetails.getUserType() != UserType.MEMBER) {
            throw new RuntimeException("購物車功能僅限會員使用");
        }
    }

    /**
     * 查詢購物車
     * GET /api/shop/cart
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartSummaryDTO>> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        validateMemberAccess(userDetails);
        Long memberId = userDetails.getUserId();
        CartSummaryDTO cart = cartService.getCart(memberId);

        return ResponseEntity.ok(new ApiResponse<>(
                true, "CART_RETRIEVED", "購物車查詢成功", cart
        ));
    }

    /**
     * 加入商品至購物車
     * POST /api/shop/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddToCartRequest request) {
        try {
            validateMemberAccess(userDetails);
            Long memberId = userDetails.getUserId();
            CartItemDTO item = cartService.addToCart(memberId, request);

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "ITEM_ADDED", "商品已加入購物車", item
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(
                    "ADD_FAILED",
                    e.getMessage()
            ));
        }
    }

    /**
     * 更新購物車項目數量
     * POST /api/shop/cart/item/update
     */
    @PostMapping("/item/update")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateCartItemRequest request) {
        try {
            validateMemberAccess(userDetails);
            Long memberId = userDetails.getUserId();
            CartItemDTO item = cartService.updateQuantity(memberId, request.getProductId(), request.getQuantity());

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "QUANTITY_UPDATED", "數量已更新", item
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(
                    "UPDATE_FAILED",
                    e.getMessage()
            ));
        }
    }

    /**
     * 移除購物車項目
     * POST /api/shop/cart/item/remove
     */
    @PostMapping("/item/remove")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RemoveCartItemRequest request) {
        try {
            validateMemberAccess(userDetails);
            Long memberId = userDetails.getUserId();
            cartService.removeItem(memberId, request.getProductId());

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "ITEM_REMOVED", "商品已移除", null
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(
                    "REMOVE_FAILED",
                    e.getMessage()
            ));
        }
    }

    /**
     * 清空購物車
     * POST /api/shop/cart/clear
     */
    @PostMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            validateMemberAccess(userDetails);
            Long memberId = userDetails.getUserId();
            cartService.clearCart(memberId);

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "CART_CLEARED", "購物車已清空", null
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(
                    "CLEAR_FAILED",
                    e.getMessage()
            ));
        }
    }

    /**
     * 合併購物車(登入時將前端 LocalStorage 資料同步至後端)
     * POST /api/shop/cart/merge
     */
    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<CartSummaryDTO>> mergeCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MergeCartRequest request) {
        try {
            validateMemberAccess(userDetails);
            Long memberId = userDetails.getUserId();
            CartSummaryDTO cart = cartService.mergeCart(memberId, request);

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "CART_MERGED", "購物車已合併", cart
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(
                    "MERGE_FAILED",
                    e.getMessage()
            ));
        }
    }
}
