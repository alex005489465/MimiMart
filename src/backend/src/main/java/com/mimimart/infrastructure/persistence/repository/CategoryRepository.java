package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品分類 Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 查詢所有未刪除的分類 (依排序權重排序)
     */
    List<Category> findAllByDeletedAtIsNullOrderBySortOrderAsc();

    /**
     * 根據 ID 查詢未刪除的分類
     */
    Optional<Category> findByIdAndDeletedAtIsNull(Long id);

    /**
     * 檢查分類名稱是否已存在 (未刪除)
     */
    boolean existsByNameAndDeletedAtIsNull(String name);
}
