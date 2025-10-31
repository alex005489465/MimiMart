package com.mimimart.api.controller.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimimart.api.dto.cart.AddToCartRequest;
import com.mimimart.api.dto.cart.MergeCartRequest;
import com.mimimart.api.dto.cart.RemoveCartItemRequest;
import com.mimimart.api.dto.cart.UpdateCartItemRequest;
import com.mimimart.application.service.CartService;
import com.mimimart.fixtures.TestFixtures;
import com.mimimart.infrastructure.persistence.entity.Category;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.entity.Product;
import com.mimimart.infrastructure.security.CustomUserDetails;
import com.mimimart.shared.valueobject.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ShopCartController 測試類別
 * 測試購物車 API 端點（正常流程）
 *
 * @author MimiMart Development Team
 * @since 2.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("購物車 API 測試")
class ShopCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private TestFixtures testFixtures;

    private Member testMember;
    private CustomUserDetails userDetails;
    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // 建立測試用會員
        testMember = testFixtures.createTestMember(1);

        // 建立 UserDetails 用於認證
        userDetails = new CustomUserDetails(
                testMember.getId(),
                testMember.getEmail(),
                testMember.getPasswordHash(),
                Collections.emptyList(),
                UserType.MEMBER
        );

        // 建立測試用分類和商品
        testCategory = testFixtures.createTestCategory(1);
        testProduct = testFixtures.createTestProduct(testCategory.getId(), 1);
    }

    @Test
    @DisplayName("GET /api/shop/cart - 成功查詢購物車")
    void testGetCart_Success() throws Exception {
        // Given: 先加入商品到購物車
        AddToCartRequest addRequest = new AddToCartRequest();
        addRequest.setProductId(testProduct.getId());
        addRequest.setQuantity(2);
        cartService.addToCart(testMember.getId(), addRequest);

        // When & Then
        mockMvc.perform(get("/api/shop/cart")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("購物車查詢成功"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.data.items[0].productName").value(testProduct.getName()))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));
    }

    @Test
    @DisplayName("POST /api/shop/cart/add - 成功加入商品")
    void testAddToCart_Success() throws Exception {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(testProduct.getId());
        request.setQuantity(3);

        // When & Then
        mockMvc.perform(post("/api/shop/cart/add")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("商品已加入購物車"))
                .andExpect(jsonPath("$.data.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.data.quantity").value(3));
    }

    @Test
    @DisplayName("POST /api/shop/cart/item/update - 成功更新數量")
    void testUpdateQuantity_Success() throws Exception {
        // Given: 先加入商品
        AddToCartRequest addRequest = new AddToCartRequest();
        addRequest.setProductId(testProduct.getId());
        addRequest.setQuantity(2);
        cartService.addToCart(testMember.getId(), addRequest);

        // 準備更新請求
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest();
        updateRequest.setProductId(testProduct.getId());
        updateRequest.setQuantity(5);

        // When & Then
        mockMvc.perform(post("/api/shop/cart/item/update")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("數量已更新"))
                .andExpect(jsonPath("$.data.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.data.quantity").value(5));
    }

    @Test
    @DisplayName("POST /api/shop/cart/item/remove - 成功移除商品")
    void testRemoveItem_Success() throws Exception {
        // Given: 先加入商品
        AddToCartRequest addRequest = new AddToCartRequest();
        addRequest.setProductId(testProduct.getId());
        addRequest.setQuantity(2);
        cartService.addToCart(testMember.getId(), addRequest);

        // 準備移除請求
        RemoveCartItemRequest removeRequest = new RemoveCartItemRequest();
        removeRequest.setProductId(testProduct.getId());

        // When & Then
        mockMvc.perform(post("/api/shop/cart/item/remove")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("商品已移除"));
    }

    @Test
    @DisplayName("POST /api/shop/cart/clear - 成功清空購物車")
    void testClearCart_Success() throws Exception {
        // Given: 先加入商品
        AddToCartRequest addRequest = new AddToCartRequest();
        addRequest.setProductId(testProduct.getId());
        addRequest.setQuantity(2);
        cartService.addToCart(testMember.getId(), addRequest);

        // When & Then
        mockMvc.perform(post("/api/shop/cart/clear")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("購物車已清空"));
    }

    @Test
    @DisplayName("POST /api/shop/cart/merge - 成功合併購物車")
    void testMergeCart_Success() throws Exception {
        // Given: 會員購物車先有一個商品
        Product product1 = testProduct;
        AddToCartRequest addRequest = new AddToCartRequest();
        addRequest.setProductId(product1.getId());
        addRequest.setQuantity(2);
        cartService.addToCart(testMember.getId(), addRequest);

        // 建立另一個商品給訪客購物車使用
        Product product2 = testFixtures.createTestProduct(testCategory.getId(), 2);

        // 準備訪客購物車資料
        MergeCartRequest mergeRequest = new MergeCartRequest();
        List<MergeCartRequest.MergeCartItem> guestItems = new ArrayList<>();

        MergeCartRequest.MergeCartItem item1 = new MergeCartRequest.MergeCartItem();
        item1.setProductId(product1.getId());
        item1.setQuantity(3); // 與會員購物車重複，數量應累加

        MergeCartRequest.MergeCartItem item2 = new MergeCartRequest.MergeCartItem();
        item2.setProductId(product2.getId());
        item2.setQuantity(1); // 新商品

        guestItems.add(item1);
        guestItems.add(item2);
        mergeRequest.setItems(guestItems);

        // When & Then
        mockMvc.perform(post("/api/shop/cart/merge")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mergeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("購物車已合併"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2));
    }
}
