package com.group1.production_service.service;

import com.group1.production_service.dto.request.UpdateCategoryRequest;
import com.group1.production_service.dto.response.CategoryResponse;
import com.group1.production_service.entity.Category;
import com.group1.production_service.exception.BadRequestException;
import com.group1.production_service.repository.CategoryRepository;
import com.group1.production_service.repository.ProductRepository;
import com.group1.production_service.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void updateCategoryRejectsBlankName() {
        when(categoryRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(category(2L, "SUB", "sub")));

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("   ");

        assertThatThrownBy(() -> categoryService.updateCategory(2L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Category name is required");
    }

    @Test
    void updateCategoryKeepsExistingParentWhenParentIdIsOmitted() {
        Category parent = category(1L, "ROOT", "root");
        Category child = category(2L, "SUB", "sub");
        child.setParent(parent);

        when(categoryRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(child));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.countByCategory_IdAndDeletedFalse(2L)).thenReturn(0L);

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setDescription("Updated description");

        CategoryResponse response = categoryService.updateCategory(2L, request);

        assertThat(child.getParent()).isSameAs(parent);
        assertThat(response.getParentId()).isEqualTo(1L);
        assertThat(response.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void deleteCategoryRejectsWhenProductsExist() {
        when(categoryRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(category(2L, "SUB", "sub")));
        when(productRepository.existsByCategory_IdAndDeletedFalse(2L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot delete category with products");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategoryRejectsWhenSubcategoriesExist() {
        when(categoryRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(category(2L, "SUB", "sub")));
        when(productRepository.existsByCategory_IdAndDeletedFalse(2L)).thenReturn(false);
        when(categoryRepository.countByParent_IdAndDeletedFalse(2L)).thenReturn(1L);

        assertThatThrownBy(() -> categoryService.deleteCategory(2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot delete category with subcategories");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    private Category category(Long id, String name, String slug) {
        return Category.builder()
                .id(id)
                .name(name)
                .slug(slug)
                .active(Boolean.TRUE)
                .deleted(Boolean.FALSE)
                .displayOrder(0)
                .build();
    }
}
