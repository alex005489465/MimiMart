package com.mimimart.application.service;

import com.mimimart.domain.product.exception.CategoryNotFoundException;
import com.mimimart.domain.product.exception.InvalidPriceException;
import com.mimimart.domain.product.exception.ProductAlreadyExistsException;
import com.mimimart.domain.product.exception.ProductNotFoundException;
import com.mimimart.infrastructure.persistence.entity.Product;
import com.mimimart.infrastructure.persistence.repository.CategoryRepository;
import com.mimimart.infrastructure.persistence.repository.ProductRepository;
import com.mimimart.shared.valueobject.Price;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 商品應用服務
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 前台: 查詢商品列表 (已上架且未刪除)
     */
    public Page<Product> getPublishedProducts(Pageable pageable) {
        return productRepository.findAllByIsPublishedTrueAndIsDeletedFalse(pageable);
    }

    /**
     * 前台: 根據分類查詢商品列表
     */
    public Page<Product> getPublishedProductsByCategory(Long categoryId, Pageable pageable) {
        // 驗證分類是否存在
        categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        return productRepository.findAllByCategoryIdAndIsPublishedTrueAndIsDeletedFalse(categoryId, pageable);
    }

    /**
     * 前台: 搜尋商品
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 前台: 根據 ID 查詢商品詳情 (已上架)
     */
    public Product getPublishedProductById(Long id) {
        return productRepository.findByIdAndIsPublishedTrueAndIsDeletedFalse(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * 後台: 查詢所有商品列表
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAllByIsDeletedFalse(pageable);
    }

    /**
     * 後台: 根據上架狀態查詢商品列表
     */
    public Page<Product> getProductsByPublishStatus(Boolean isPublished, Pageable pageable) {
        if (isPublished == null) {
            return getAllProducts(pageable);
        }
        return productRepository.findAllByIsPublishedAndIsDeletedFalse(isPublished, pageable);
    }

    /**
     * 後台: 根據 ID 查詢商品詳情
     */
    public Product getProductById(Long id) {
        return productRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * 後台: 新增商品
     */
    @Transactional
    public Product createProduct(String name, String description, BigDecimal price,
                                 BigDecimal originalPrice, String imageUrl, Long categoryId) {
        // 驗證價格
        Price priceObj = originalPrice != null ? Price.of(price, originalPrice) : Price.of(price);

        // 檢查商品名稱是否已存在
        if (productRepository.existsByNameAndIsDeletedFalse(name)) {
            throw new ProductAlreadyExistsException(name);
        }

        // 驗證分類是否存在
        categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        // 建立商品
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(priceObj.getPrice());
        product.setOriginalPrice(priceObj.getOriginalPrice());
        product.setImageUrl(imageUrl);
        product.setCategoryId(categoryId);
        product.setIsPublished(true);  // 預設已上架
        product.setIsDeleted(false);

        return productRepository.save(product);
    }

    /**
     * 後台: 更新商品
     */
    @Transactional
    public Product updateProduct(Long id, String name, String description, BigDecimal price,
                                BigDecimal originalPrice, String imageUrl, Long categoryId) {
        // 檢查商品是否存在
        Product product = getProductById(id);

        // 驗證價格
        Price priceObj = originalPrice != null ? Price.of(price, originalPrice) : Price.of(price);

        // 檢查新名稱是否與其他商品重複
        if (!product.getName().equals(name) && productRepository.existsByNameAndIsDeletedFalse(name)) {
            throw new ProductAlreadyExistsException(name);
        }

        // 驗證分類是否存在
        categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        // 更新商品資訊
        product.setName(name);
        product.setDescription(description);
        product.setPrice(priceObj.getPrice());
        product.setOriginalPrice(priceObj.getOriginalPrice());
        product.setImageUrl(imageUrl);
        product.setCategoryId(categoryId);

        return productRepository.save(product);
    }

    /**
     * 後台: 刪除商品 (軟刪除)
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.markAsDeleted();
        productRepository.save(product);
    }

    /**
     * 後台: 上架商品
     */
    @Transactional
    public Product publishProduct(Long id) {
        Product product = getProductById(id);

        try {
            product.publish();
        } catch (IllegalStateException e) {
            throw new InvalidPriceException(e.getMessage());
        }

        return productRepository.save(product);
    }

    /**
     * 後台: 下架商品
     */
    @Transactional
    public Product unpublishProduct(Long id) {
        Product product = getProductById(id);
        product.unpublish();
        return productRepository.save(product);
    }
}
