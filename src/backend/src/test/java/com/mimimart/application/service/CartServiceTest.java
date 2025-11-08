package com.mimimart.application.service;

import com.mimimart.api.dto.cart.*;
import com.mimimart.fixtures.TestFixtures;
import com.mimimart.infrastructure.persistence.entity.Category;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CartService 測試類別
 * 測試購物車相關功能（正常流程）
 *
 * @author MimiMart Development Team
 * @since 2.0.0
 */
@SpringBootTest
@Transactional
@DisplayName("購物車服務測試")
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private TestFixtures testFixtures;

    @Test
    @DisplayName("查詢購物車 - 成功取得含多個商品的購物車")
    void testGetCart_WithMultipleItems() {
        // Given: 建立測試資料
        Member member = testFixtures.createTestMember(1);
        Category category = testFixtures.createTestCategory(1);
        Product product1 = testFixtures.createTestProduct(category.getId(), 1);
        Product product2 = testFixtures.createTestProduct(category.getId(), 2);

        // 加入兩個商品到購物車
        AddToCartRequest request1 = new AddToCartRequest();
        request1.setProductId(product1.getId());
        request1.setQuantity(2);
        cartService.addToCart(member.getId(), request1);

        AddToCartRequest request2 = new AddToCartRequest();
        request2.setProductId(product2.getId());
        request2.setQuantity(3);
        cartService.addToCart(member.getId(), request2);

        // When: 查詢購物車
        CartSummaryDTO cart = cartService.getCart(member.getId());

        // Then: 驗證購物車內容
        assertNotNull(cart);
        assertEquals(2, cart.getItems().size());

        // 驗證商品1
        CartItemDTO item1 = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(product1.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(item1);
        assertEquals(product1.getName(), item1.getProductName());
        assertEquals(product1.getPrice(), item1.getPrice());
        assertEquals(2, item1.getQuantity());

        // 驗證商品2
        CartItemDTO item2 = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(product2.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(item2);
        assertEquals(product2.getName(), item2.getProductName());
        assertEquals(product2.getPrice(), item2.getPrice());
        assertEquals(3, item2.getQuantity());
    }

    @Test
    @DisplayName("加入商品 - 成功加入新商品至空購物車")
    void testAddToCart_NewProduct() {
        // Given: 建立測試資料
        Member member = testFixtures.createTestMember(2);
        Category category = testFixtures.createTestCategory(2);
        Product product = testFixtures.createTestProduct(category.getId(), 1);

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(product.getId());
        request.setQuantity(3);

        // When: 加入商品到購物車
        CartItemDTO item = cartService.addToCart(member.getId(), request);

        // Then: 驗證購物車項目建立成功
        assertNotNull(item);
        assertEquals(product.getId(), item.getProductId());
        assertEquals(product.getName(), item.getProductName());
        assertEquals(product.getPrice(), item.getPrice());
        assertEquals(3, item.getQuantity());
    }

    @Test
    @DisplayName("加入商品 - 成功累加已存在商品的數量")
    void testAddToCart_ExistingProduct() {
        // Given: 建立測試資料並先加入一次
        Member member = testFixtures.createTestMember(3);
        Category category = testFixtures.createTestCategory(3);
        Product product = testFixtures.createTestProduct(category.getId(), 1);

        AddToCartRequest request1 = new AddToCartRequest();
        request1.setProductId(product.getId());
        request1.setQuantity(2);
        cartService.addToCart(member.getId(), request1);

        // When: 再次加入相同商品
        AddToCartRequest request2 = new AddToCartRequest();
        request2.setProductId(product.getId());
        request2.setQuantity(3);
        CartItemDTO item = cartService.addToCart(member.getId(), request2);

        // Then: 驗證數量累加
        assertNotNull(item);
        assertEquals(product.getId(), item.getProductId());
        assertEquals(5, item.getQuantity()); // 2 + 3 = 5
    }

    @Test
    @DisplayName("更新數量 - 成功更新購物車項目數量")
    void testUpdateQuantity_Success() {
        // Given: 建立測試資料並加入商品
        Member member = testFixtures.createTestMember(4);
        Category category = testFixtures.createTestCategory(4);
        Product product = testFixtures.createTestProduct(category.getId(), 1);

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(product.getId());
        request.setQuantity(2);
        cartService.addToCart(member.getId(), request);

        // When: 更新數量
        UpdateCartItemRequest updateRequest = new UpdateCartItemRequest();
        updateRequest.setProductId(product.getId());
        updateRequest.setQuantity(5);
        CartItemDTO updatedItem = cartService.updateQuantity(member.getId(), updateRequest);

        // Then: 驗證數量更新成功
        assertNotNull(updatedItem);
        assertEquals(product.getId(), updatedItem.getProductId());
        assertEquals(5, updatedItem.getQuantity());
    }

    @Test
    @DisplayName("移除商品 - 成功移除購物車中的商品")
    void testRemoveItem_Success() {
        // Given: 建立測試資料並加入商品
        Member member = testFixtures.createTestMember(5);
        Category category = testFixtures.createTestCategory(5);
        Product product = testFixtures.createTestProduct(category.getId(), 1);

        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(product.getId());
        request.setQuantity(2);
        cartService.addToCart(member.getId(), request);

        // When: 移除商品
        RemoveCartItemRequest removeRequest = new RemoveCartItemRequest();
        removeRequest.setProductId(product.getId());
        cartService.removeItem(member.getId(), removeRequest);

        // Then: 驗證購物車為空
        CartSummaryDTO cart = cartService.getCart(member.getId());
        assertNotNull(cart);
        assertEquals(0, cart.getItems().size());
    }

    @Test
    @DisplayName("清空購物車 - 成功清空所有項目")
    void testClearCart_Success() {
        // Given: 建立測試資料並加入多個商品
        Member member = testFixtures.createTestMember(6);
        Category category = testFixtures.createTestCategory(6);
        Product product1 = testFixtures.createTestProduct(category.getId(), 1);
        Product product2 = testFixtures.createTestProduct(category.getId(), 2);

        AddToCartRequest request1 = new AddToCartRequest();
        request1.setProductId(product1.getId());
        request1.setQuantity(2);
        cartService.addToCart(member.getId(), request1);

        AddToCartRequest request2 = new AddToCartRequest();
        request2.setProductId(product2.getId());
        request2.setQuantity(3);
        cartService.addToCart(member.getId(), request2);

        // When: 清空購物車
        cartService.clearCart(member.getId());

        // Then: 驗證購物車為空
        CartSummaryDTO cart = cartService.getCart(member.getId());
        assertNotNull(cart);
        assertEquals(0, cart.getItems().size());
    }

    @Test
    @DisplayName("合併購物車 - 無重複商品")
    void testMergeCart_NoOverlap() {
        // Given: 建立測試資料
        Member member = testFixtures.createTestMember(7);
        Category category = testFixtures.createTestCategory(7);
        Product product1 = testFixtures.createTestProduct(category.getId(), 1);
        Product product2 = testFixtures.createTestProduct(category.getId(), 2);

        // 會員購物車有商品1
        AddToCartRequest request1 = new AddToCartRequest();
        request1.setProductId(product1.getId());
        request1.setQuantity(2);
        cartService.addToCart(member.getId(), request1);

        // 準備訪客購物車資料（有商品2）
        MergeCartRequest mergeRequest = new MergeCartRequest();
        List<MergeCartRequest.MergeCartItem> guestItems = new ArrayList<>();
        MergeCartRequest.MergeCartItem guestItem = new MergeCartRequest.MergeCartItem();
        guestItem.setProductId(product2.getId());
        guestItem.setQuantity(3);
        guestItems.add(guestItem);
        mergeRequest.setItems(guestItems);

        // When: 合併購物車
        CartSummaryDTO cart = cartService.mergeCart(member.getId(), mergeRequest);

        // Then: 驗證合併後有2個商品
        assertNotNull(cart);
        assertEquals(2, cart.getItems().size());

        // 驗證商品1數量不變
        CartItemDTO item1 = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(product1.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(item1);
        assertEquals(2, item1.getQuantity());

        // 驗證商品2被加入
        CartItemDTO item2 = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(product2.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(item2);
        assertEquals(3, item2.getQuantity());
    }

    @Test
    @DisplayName("合併購物車 - 有重複商品")
    void testMergeCart_WithOverlap() {
        // Given: 建立測試資料
        Member member = testFixtures.createTestMember(8);
        Category category = testFixtures.createTestCategory(8);
        Product product = testFixtures.createTestProduct(category.getId(), 1);

        // 會員購物車有商品（數量2）
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(product.getId());
        request.setQuantity(2);
        cartService.addToCart(member.getId(), request);

        // 準備訪客購物車資料（相同商品，數量3）
        MergeCartRequest mergeRequest = new MergeCartRequest();
        List<MergeCartRequest.MergeCartItem> guestItems = new ArrayList<>();
        MergeCartRequest.MergeCartItem guestItem = new MergeCartRequest.MergeCartItem();
        guestItem.setProductId(product.getId());
        guestItem.setQuantity(3);
        guestItems.add(guestItem);
        mergeRequest.setItems(guestItems);

        // When: 合併購物車
        CartSummaryDTO cart = cartService.mergeCart(member.getId(), mergeRequest);

        // Then: 驗證合併後數量累加
        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());

        CartItemDTO item = cart.getItems().get(0);
        assertEquals(product.getId(), item.getProductId());
        assertEquals(5, item.getQuantity()); // 2 + 3 = 5
    }
}
