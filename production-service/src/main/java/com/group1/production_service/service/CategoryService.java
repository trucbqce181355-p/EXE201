package com.group1.production_service.service;

import com.group1.production_service.dto.request.CategoryReorderRequest;
import com.group1.production_service.dto.request.CreateCategoryRequest;
import com.group1.production_service.dto.request.UpdateCategoryRequest;
import com.group1.production_service.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest request);

    List<CategoryResponse> getCategories(Long parentId, Boolean isActive);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);

    List<CategoryResponse> reorderCategories(List<CategoryReorderRequest> requests);
}
