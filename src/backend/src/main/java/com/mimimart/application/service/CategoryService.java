package com.mimimart.application.service;

import com.mimimart.domain.product.exception.CategoryAlreadyExistsException;
import com.mimimart.domain.product.exception.CategoryNotFoundException;
import com.mimimart.infrastructure.persistence.entity.Category;
import com.mimimart.infrastructure.persistence.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分類應用服務
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 查詢所有分類列表 (未刪除,依排序權重排序)
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByDeletedAtIsNullOrderBySortOrderAsc();
    }

    /**
     * 根據 ID 查詢分類詳情
     */
    public Category getCategoryById(Long id) {
        return categoryRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    /**
     * 新增分類
     */
    @Transactional
    public Category createCategory(String name, String description, Integer sortOrder) {
        // 檢查分類名稱是否已存在
        if (categoryRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new CategoryAlreadyExistsException(name);
        }

        // 建立分類
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setSortOrder(sortOrder != null ? sortOrder : 0);

        return categoryRepository.save(category);
    }

    /**
     * 更新分類
     */
    @Transactional
    public Category updateCategory(Long id, String name, String description, Integer sortOrder) {
        // 檢查分類是否存在
        Category category = getCategoryById(id);

        // 檢查新名稱是否與其他分類重複
        if (!category.getName().equals(name) && categoryRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new CategoryAlreadyExistsException(name);
        }

        // 更新資訊
        category.setName(name);
        category.setDescription(description);
        if (sortOrder != null) {
            category.setSortOrder(sortOrder);
        }

        return categoryRepository.save(category);
    }

    /**
     * 刪除分類 (軟刪除)
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        category.markAsDeleted();
        categoryRepository.save(category);
    }
}
