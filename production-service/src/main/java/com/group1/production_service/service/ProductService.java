package com.group1.production_service.service;

import com.group1.production_service.dto.request.AssignProductCategoryRequest;
import com.group1.production_service.dto.request.AssignProductTagsRequest;
import com.group1.production_service.dto.request.BulkCategoryAssignmentRequest;
import com.group1.production_service.dto.request.CreateProductRequest;
import com.group1.production_service.dto.request.UpdateProductRequest;
import com.group1.production_service.dto.request.TagRequest;
import com.group1.production_service.dto.response.*;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);

    PageResponse<ProductListItemResponse> getProducts(
            int page,
            int limit,
            Long categoryId,
            Boolean isAvailable,
            BigDecimal priceMin,
            BigDecimal priceMax,
            String search,
            String sort,
            String order);

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    void deleteProduct(Long id);

    ProductResponse duplicateProduct(Long id);

    ProductResponse assignCategory(Long id, AssignProductCategoryRequest request);

    ProductResponse assignTags(Long id, AssignProductTagsRequest request);

    void bulkAssignCategory(BulkCategoryAssignmentRequest request);

    void removeFromCategory(Long id);

    List<CategoryResponse> getAllCategoriesForDropdown();

    List<TagResponse> getProductTags(Long productId);

    List<TagResponse> getAllTags();

    TagResponse createTag(TagRequest request);

    TagResponse updateTag(Long id, TagRequest request);

    void deleteTag(Long id);
}
