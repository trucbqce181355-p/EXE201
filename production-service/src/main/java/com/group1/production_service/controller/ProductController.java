package com.group1.production_service.controller;

import com.group1.production_service.dto.request.AssignProductCategoryRequest;
import com.group1.production_service.dto.request.AssignProductTagsRequest;
import com.group1.production_service.dto.request.BulkCategoryAssignmentRequest;
import com.group1.production_service.dto.request.CreateProductRequest;
import com.group1.production_service.dto.request.UpdateProductRequest;
import com.group1.production_service.dto.request.TagRequest;
import com.group1.production_service.dto.response.*;
import com.group1.production_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT:CREATE')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse data = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Product created", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductListItemResponse>>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(name = "category_id", required = false) Long categoryId,
            @RequestParam(name = "is_available", required = false) Boolean isAvailable,
            @RequestParam(name = "price_min", required = false) BigDecimal priceMin,
            @RequestParam(name = "price_max", required = false) BigDecimal priceMax,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "updated_at") String sort,
            @RequestParam(defaultValue = "DESC") String order) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Products fetched",
            productService.getProducts(page, limit, categoryId, isAvailable, priceMin, priceMax, search, sort, order)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Product fetched", productService.getProductById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Product updated", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product deleted", null));
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasAuthority('PRODUCT:CREATE')")
    public ResponseEntity<ApiResponse<ProductResponse>> duplicateProduct(@PathVariable Long id) {
        ProductResponse data = productService.duplicateProduct(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Product duplicated", data));
    }

    @PutMapping("/{id}/category")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<ProductResponse>> assignCategory(
            @PathVariable Long id,
            @Valid @RequestBody AssignProductCategoryRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Product category updated", productService.assignCategory(id, request)));
    }

    @PostMapping("/{id}/tags")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<ProductResponse>> assignTags(
            @PathVariable Long id,
            @Valid @RequestBody AssignProductTagsRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Product tags updated", productService.assignTags(id, request)));
    }

    @PutMapping("/bulk/category")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<Void>> bulkAssignCategory(@Valid @RequestBody BulkCategoryAssignmentRequest request) {
        productService.bulkAssignCategory(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product categories updated", null));
    }

    @DeleteMapping("/{id}/category")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<Void>> removeFromCategory(@PathVariable Long id) {
        productService.removeFromCategory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product removed from category", null));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('PRODUCT:READ')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(new ApiResponse<>(true,
                "Categories fetched", productService.getAllCategoriesForDropdown()));
    }

    @GetMapping("/{id}/tags")
    @PreAuthorize("hasAuthority('PRODUCT:READ')")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getProductTags(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true,
                "Product tags fetched", productService.getProductTags(id)));
    }

    @GetMapping("/tags")
    @PreAuthorize("hasAuthority('PRODUCT:READ')")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags() {
        return ResponseEntity.ok(new ApiResponse<>(true,
                "Tags fetched", productService.getAllTags()));
    }

    @PostMapping("/tags")
    @PreAuthorize("hasAuthority('PRODUCT:CREATE')")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tag created", productService.createTag(request)));
    }

    @PutMapping("/tags/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tag updated", productService.updateTag(id, request)));
    }

    @DeleteMapping("/tags/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        productService.deleteTag(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tag deleted", null));
    }
}
