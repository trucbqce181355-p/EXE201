package com.group1.production_service.controller;

import com.group1.production_service.dto.request.CategoryReorderRequest;
import com.group1.production_service.dto.request.CreateCategoryRequest;
import com.group1.production_service.dto.request.UpdateCategoryRequest;
import com.group1.production_service.dto.response.ApiResponse;
import com.group1.production_service.dto.response.CategoryResponse;
import com.group1.production_service.exception.BadRequestException;
import com.group1.production_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY:CREATE')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse data = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Category created", data));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CATEGORY:READ')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @RequestParam(name = "parent_id", required = false) String parentIdParam,
            @RequestParam(name = "is_active", required = false) Boolean isActive) {
        Long parentId = parseParentIdFilter(parentIdParam);
        return ResponseEntity.ok(new ApiResponse<>(true, "Categories fetched", categoryService.getCategories(parentId, isActive)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY:READ')")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Category fetched", categoryService.getCategoryById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY:UPDATE')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Category updated", categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY:DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Category deleted", null));
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasAuthority('CATEGORY:UPDATE')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> reorderCategories(
            @Valid @RequestBody List<CategoryReorderRequest> requests) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Categories reordered", categoryService.reorderCategories(requests)));
    }

    private Long parseParentIdFilter(String parentIdParam) {
        if (!StringUtils.hasText(parentIdParam) || "null".equalsIgnoreCase(parentIdParam.trim())) {
            return null;
        }

        try {
            return Long.valueOf(parentIdParam.trim());
        } catch (NumberFormatException ex) {
            throw new BadRequestException("parent_id must be a number or null");
        }
    }
}
