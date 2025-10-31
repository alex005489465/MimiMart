package com.mimimart.application.service;

import com.mimimart.domain.cart.model.Cart;
import com.mimimart.domain.cart.service.CartMergeService;
import com.mimimart.infrastructure.persistence.mapper.CartMapper;
import com.mimimart.api.dto.cart.*;
import com.mimimart.infrastructure.persistence.entity.CartItem;
import com.mimimart.infrastructure.persistence.entity.Product;
import com.mimimart.infrastructure.persistence.repository.CartItemRepository;
import com.mimimart.infrastructure.persistence.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 購物車應用服務 (重構為協調層)
 * 職責: 協調領域模型、Repository、Mapper
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (DDD Refactoring Phase 3.1)
 */
@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartMergeService cartMergeService;

    /**
     * 查詢會員的購物車摘要(含商品完整資訊)
     */
    public CartSummaryDTO getCart(Long memberId) {
        List<CartItem> cartItems = cartItemRepository.findByMemberId(memberId);

        List<CartItemDTO> itemDTOs = cartItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new CartSummaryDTO(itemDTOs);
    }

    /**
     * 加入商品至購物車
     * - 若商品已存在,則累加數量
     * - 若商品不存在,則新增項目
     */
    @Transactional
    public CartItemDTO addToCart(Long memberId, AddToCartRequest request) {
        // 1. 驗證商品是否存在
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // TODO: 檢查庫存(待 Product 新增 stock 欄位後實作)

        // 2. 載入購物車領域模型
        List<CartItem> cartItemEntities = cartItemRepository.findByMemberId(memberId);
        Cart cart = cartMapper.toDomain(cartItemEntities, memberId);

        // 3. 使用領域模型處理業務邏輯
        cart.addProduct(request.getProductId(), request.getQuantity());

        // 4. 持久化:查找或建立 JPA 實體
        CartItem cartItemEntity = cartItemRepository
                .findByMemberIdAndProductId(memberId, request.getProductId())
                .orElse(new CartItem(memberId, request.getProductId(), 0));

        cartItemEntity.setQuantity(cart.getQuantity(request.getProductId()));
        cartItemEntity = cartItemRepository.save(cartItemEntity);

        // 5. 回傳 DTO
        return convertToDTO(cartItemEntity);
    }

    /**
     * 更新購物車項目數量
     */
    @Transactional
    public CartItemDTO updateQuantity(Long memberId, Long productId, Integer quantity) {
        // 1. 驗證商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 2. 查詢購物車項目並驗證權限
        CartItem cartItemEntity = cartItemRepository.findByMemberIdAndProductId(memberId, productId)
                .orElseThrow(() -> new RuntimeException("購物車中未找到該商品"));

        // TODO: 檢查庫存(待 Product 新增 stock 欄位後實作)

        // 3. 載入購物車領域模型
        List<CartItem> cartItemEntities = cartItemRepository.findByMemberId(memberId);
        Cart cart = cartMapper.toDomain(cartItemEntities, memberId);

        // 4. 使用領域模型處理業務邏輯
        cart.updateQuantity(productId, quantity);

        // 5. 更新實體
        cartItemEntity.setQuantity(quantity);
        cartItemEntity = cartItemRepository.save(cartItemEntity);

        // 6. 回傳 DTO
        return convertToDTO(cartItemEntity);
    }

    /**
     * 移除購物車項目
     */
    @Transactional
    public void removeItem(Long memberId, Long productId) {
        // 1. 查詢購物車項目並驗證權限
        CartItem cartItemEntity = cartItemRepository.findByMemberIdAndProductId(memberId, productId)
                .orElseThrow(() -> new RuntimeException("購物車中未找到該商品"));

        // 2. 載入購物車領域模型
        List<CartItem> cartItemEntities = cartItemRepository.findByMemberId(memberId);
        Cart cart = cartMapper.toDomain(cartItemEntities, memberId);

        // 3. 使用領域模型處理業務邏輯
        cart.removeProduct(productId);

        // 4. 刪除實體
        cartItemRepository.delete(cartItemEntity);
    }

    /**
     * 清空購物車
     */
    @Transactional
    public void clearCart(Long memberId) {
        cartItemRepository.deleteByMemberId(memberId);
    }

    /**
     * 合併購物車(登入時將前端 LocalStorage 資料同步至後端)
     * 合併策略:若商品已存在於後端購物車,則取較大數量或累加
     */
    @Transactional
    public CartSummaryDTO mergeCart(Long memberId, MergeCartRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return getCart(memberId);
        }

        // 1. 載入會員購物車領域模型
        List<CartItem> memberCartEntities = cartItemRepository.findByMemberId(memberId);
        Cart memberCart = cartMapper.toDomain(memberCartEntities, memberId);

        // 2. 建立訪客購物車領域模型
        Cart guestCart = Cart.create(0L); // 訪客 ID 使用 0
        for (MergeCartRequest.MergeCartItem item : request.getItems()) {
            // 驗證商品是否存在
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品 ID " + item.getProductId() + " 不存在"));

            guestCart.addProduct(item.getProductId(), item.getQuantity());
        }

        // 3. 使用領域服務合併購物車
        Cart mergedCart = cartMergeService.merge(memberCart, guestCart);

        // 4. 持久化合併後的購物車
        // 刪除原有購物車
        cartItemRepository.deleteByMemberId(memberId);

        // 儲存合併後的購物車
        for (com.mimimart.domain.cart.model.CartItem item : mergedCart.getItems()) {
            CartItem entity = new CartItem(memberId, item.getProductId(), item.getQuantity());
            cartItemRepository.save(entity);
        }

        // 5. 回傳購物車摘要
        return getCart(memberId);
    }

    /**
     * 將 CartItem 實體轉換為 DTO(JOIN Product 取得完整資訊)
     */
    private CartItemDTO convertToDTO(CartItem cartItem) {
        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // TODO: 未來實作價格變動功能時,需比對 snapshot_price 與 product.getPrice()
        // 若不一致,應在 DTO 中標示價格已變動

        return new CartItemDTO(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                cartItem.getQuantity()
        );
    }
}
