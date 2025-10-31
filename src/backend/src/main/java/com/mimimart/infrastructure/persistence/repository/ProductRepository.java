package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商品 Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 前台: 查詢已上架且未刪除的商品列表 (分頁)
     */
    Page<Product> findAllByIsPublishedTrueAndIsDeletedFalse(Pageable pageable);

    /**
     * 前台: 根據分類查詢已上架且未刪除的商品列表 (分頁)
     */
    Page<Product> findAllByCategoryIdAndIsPublishedTrueAndIsDeletedFalse(Long categoryId, Pageable pageable);

    /**
     * 前台: 根據 ID 查詢已上架且未刪除的商品
     */
    Optional<Product> findByIdAndIsPublishedTrueAndIsDeletedFalse(Long id);

    /**
     * 前台: 搜尋商品 (名稱模糊查詢,已上架且未刪除)
     */
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% AND p.isPublished = true AND p.isDeleted = false")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 後台: 查詢所有商品 (分頁)
     */
    Page<Product> findAllByIsDeletedFalse(Pageable pageable);

    /**
     * 後台: 根據上架狀態查詢商品 (分頁)
     */
    Page<Product> findAllByIsPublishedAndIsDeletedFalse(Boolean isPublished, Pageable pageable);

    /**
     * 後台: 根據 ID 查詢未刪除的商品
     */
    Optional<Product> findByIdAndIsDeletedFalse(Long id);

    /**
     * 檢查商品名稱是否已存在 (未刪除)
     */
    boolean existsByNameAndIsDeletedFalse(String name);
}
