package com.group1.production_service.service.impl;

import com.group1.production_service.dto.request.CategoryReorderRequest;
import com.group1.production_service.dto.request.CreateCategoryRequest;
import com.group1.production_service.dto.request.UpdateCategoryRequest;
import com.group1.production_service.dto.response.CategoryResponse;
import com.group1.production_service.entity.Category;
import com.group1.production_service.exception.BadRequestException;
import com.group1.production_service.exception.ConflictException;
import com.group1.production_service.exception.ResourceNotFoundException;
import com.group1.production_service.repository.CategoryRepository;
import com.group1.production_service.repository.ProductRepository;
import com.group1.production_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        validateCategorySlug(request.getSlug(), null);
        Category parent = resolveParent(request.getParentId(), null);

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(request.getSlug().trim())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .parent(parent)
                .displayOrder(request.getDisplayOrder() == null ? 0 : request.getDisplayOrder())
                .active(request.getActive() == null ? Boolean.TRUE : request.getActive())
                .build();

        return mapCategory(categoryRepository.save(category), true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Long parentId, Boolean isActive) {
        List<Category> categories = categoryRepository.findAllByDeletedFalseOrderByDisplayOrderAscIdAsc();
        return categories.stream()
                .filter(category -> parentId == null ? category.getParent() == null : hasParent(category, parentId))
                .filter(category -> matchesActiveFilter(category, isActive))
                .map(category -> mapCategory(category, true, isActive))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        return mapCategory(getCategoryEntity(id), true);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = getCategoryEntity(id);
        validateUpdateRequest(request);

        if (StringUtils.hasText(request.getName())) {
            category.setName(request.getName().trim());
        }

        if (StringUtils.hasText(request.getSlug()) && !request.getSlug().trim().equalsIgnoreCase(category.getSlug())) {
            if (productRepository.existsByCategory_IdAndDeletedFalse(id)) {
                throw new BadRequestException("Cannot change category slug while products are linked");
            }
            validateCategorySlug(request.getSlug(), id);
            category.setSlug(request.getSlug().trim());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        if (request.getParentId() != null) {
            category.setParent(resolveParent(request.getParentId(), id));
        }

        return mapCategory(categoryRepository.save(category), true);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryEntity(id);
        if (productRepository.existsByCategory_IdAndDeletedFalse(id)) {
            throw new BadRequestException("Cannot delete category with products");
        }
        if (categoryRepository.countByParent_IdAndDeletedFalse(id) > 0) {
            throw new BadRequestException("Cannot delete category with subcategories");
        }

        category.setDeleted(true);
        category.setActive(false);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public List<CategoryResponse> reorderCategories(List<CategoryReorderRequest> requests) {
        List<Category> categories = requests.stream()
                .map(request -> {
                    Category category = getCategoryEntity(request.getId());
                    category.setDisplayOrder(request.getDisplayOrder());
                    return category;
                })
                .toList();

        categoryRepository.saveAll(categories);
        return categories.stream().map(category -> mapCategory(category, false)).toList();
    }

    private Category getCategoryEntity(Long id) {
        return categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private void validateCategorySlug(String slug, Long currentId) {
        String normalizedSlug = slug == null ? null : slug.trim();
        if (!StringUtils.hasText(normalizedSlug)) {
            throw new BadRequestException("Category slug is required");
        }

        boolean exists = currentId == null
                ? categoryRepository.existsBySlugIgnoreCase(normalizedSlug)
                : categoryRepository.existsBySlugIgnoreCaseAndIdNot(normalizedSlug, currentId);

        if (exists) {
            throw new ConflictException("Category slug already exists");
        }
    }

    private void validateUpdateRequest(UpdateCategoryRequest request) {
        if (request.getName() != null && request.getName().isBlank()) {
            throw new BadRequestException("Category name is required");
        }

        if (request.getSlug() != null && request.getSlug().isBlank()) {
            throw new BadRequestException("Category slug is required");
        }
    }

    private Category resolveParent(Long parentId, Long currentId) {
        if (parentId == null) {
            return null;
        }
        if (currentId != null && currentId.equals(parentId)) {
            throw new BadRequestException("Parent category not found");
        }
        return categoryRepository.findByIdAndDeletedFalse(parentId)
                .orElseThrow(() -> new BadRequestException("Parent category not found"));
    }

    private boolean hasParent(Category category, Long parentId) {
        return category.getParent() != null && parentId.equals(category.getParent().getId());
    }

    private boolean matchesActiveFilter(Category category, Boolean isActive) {
        return isActive == null || isActive.equals(category.getActive());
    }

    private CategoryResponse mapCategory(Category category, boolean includeChildren) {
        return mapCategory(category, includeChildren, null);
    }

    private CategoryResponse mapCategory(Category category, boolean includeChildren, Boolean activeFilter) {
        List<CategoryResponse> children = includeChildren
                ? category.getChildren().stream()
                .filter(child -> !Boolean.TRUE.equals(child.getDeleted()))
                .filter(child -> matchesActiveFilter(child, activeFilter))
                .map(child -> mapCategory(child, true, activeFilter))
                .toList()
                : List.of();

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParent() == null ? null : category.getParent().getId())
                .displayOrder(category.getDisplayOrder())
                .active(category.getActive())
                .productCount(productRepository.countByCategory_IdAndDeletedFalse(category.getId()))
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .subcategories(children)
                .build();
    }
}
