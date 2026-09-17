

package com.group1.production_service.service.impl;

import com.group1.production_service.dto.request.AssignProductCategoryRequest;
import com.group1.production_service.dto.request.AssignProductTagsRequest;
import com.group1.production_service.dto.request.BulkCategoryAssignmentRequest;
import com.group1.production_service.dto.request.CreateProductRequest;
import com.group1.production_service.dto.request.UpdateProductRequest;
import com.group1.production_service.dto.request.TagRequest;
import com.group1.production_service.dto.response.CategoryResponse;
import com.group1.production_service.dto.response.PageResponse;
import com.group1.production_service.dto.response.ProductAvailabilityItemResponse;
import com.group1.production_service.dto.response.ProductAvailabilityResponse;
import com.group1.production_service.dto.response.ProductImageResponse;
import com.group1.production_service.dto.response.ProductListItemResponse;
import com.group1.production_service.dto.response.ProductResponse;
import com.group1.production_service.dto.response.TagResponse;
import com.group1.production_service.entity.Category;
import com.group1.production_service.entity.Product;
import com.group1.production_service.entity.ProductAvailability;
import com.group1.production_service.entity.ProductImage;
import com.group1.production_service.entity.ProductStatus;
import com.group1.production_service.entity.Tag;
import com.group1.production_service.exception.BadRequestException;
import com.group1.production_service.exception.ConflictException;
import com.group1.production_service.exception.ResourceNotFoundException;
import com.group1.production_service.repository.CategoryRepository;
import com.group1.production_service.repository.ProductAvailabilityRepository;
import com.group1.production_service.repository.ProductImageRepository;
import com.group1.production_service.repository.ProductRepository;
import com.group1.production_service.repository.TagRepository;
import com.group1.production_service.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductAvailabilityRepository productAvailabilityRepository;
    private final com.group1.production_service.repository.ReviewRepository reviewRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        validateSku(request.getSku(), null);
        Category category = getActiveCategory(request.getCategoryId());

        Product product = Product.builder()
                .name(request.getName().trim())
                .sku(request.getSku().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .available(request.getAvailable() == null ? Boolean.TRUE : request.getAvailable())
                .preparationTime(request.getPreparationTime())
                .status(ProductStatus.ACTIVE)
                .build();

        return mapProduct(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductListItemResponse> getProducts(
            int page,
            int limit,
            Long categoryId,
            Boolean isAvailable,
            BigDecimal priceMin,
            BigDecimal priceMax,
            String search,
            String sort,
            String order) {

        PageRequest pageRequest = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(limit, 1),
                Sort.by(resolveDirection(order), resolveSortField(sort))
        );

        Specification<Product> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (isAvailable != null) {
                predicates.add(criteriaBuilder.equal(root.get("available"), isAvailable));
            }
            if (priceMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), priceMin));
            }
            if (priceMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), priceMax));
            }
            if (StringUtils.hasText(search)) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), keyword)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProductListItemResponse> result = productRepository.findAll(specification, pageRequest)
                .map(this::mapProductListItem);

        return PageResponse.<ProductListItemResponse>builder()
                .data(result.getContent())
                .total(result.getTotalElements())
                .page(page)
                .limit(limit)
                .totalPages(result.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return mapProduct(getProductEntity(id));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = getProductEntity(id);

        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getSku()) && !request.getSku().trim().equalsIgnoreCase(product.getSku())) {
            validateSku(request.getSku(), id);
            product.setSku(request.getSku().trim());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            product.setCategory(getActiveCategory(request.getCategoryId()));
        }
        if (request.getAvailable() != null) {
            product.setAvailable(request.getAvailable());
        }
        if (request.getPreparationTime() != null) {
            product.setPreparationTime(request.getPreparationTime());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        return mapProduct(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductEntity(id);

        // Delete related entities to prevent foreign key constraint violations
        reviewRepository.deleteAll(reviewRepository.findByProductIdOrderByCreatedAtDesc(id));
        productImageRepository.deleteAll(product.getImages());
        productAvailabilityRepository.deleteAll(product.getFranchiseAvailabilities());
        product.getTags().clear();

        // Physically delete from the database
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductResponse duplicateProduct(Long id) {
        Product source = getProductEntity(id);

        Product duplicate = Product.builder()
                .name(source.getName() + " Copy")
                .sku(generateDuplicateSku(source.getSku()))
                .description(source.getDescription())
                .price(source.getPrice())
                .category(source.getCategory())
                .available(false)
                .preparationTime(source.getPreparationTime())
                .status(ProductStatus.DRAFT)
                .availableFrom(source.getAvailableFrom())
                .availableUntil(source.getAvailableUntil())
                .autoUnavailableWhenOutOfStock(source.getAutoUnavailableWhenOutOfStock())
                .tags(new LinkedHashSet<>(source.getTags()))
                .build();

        return mapProduct(productRepository.save(duplicate));
    }

    @Override
    @Transactional
    public ProductResponse assignCategory(Long id, AssignProductCategoryRequest request) {
        Product product = getProductEntity(id);
        product.setCategory(getActiveCategory(request.getCategoryId()));
        return mapProduct(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse assignTags(Long id, AssignProductTagsRequest request) {
        Product product = getProductEntity(id);

        if (request.getTagIds().isEmpty()) {
            product.setTags(new LinkedHashSet<>());
            return mapProduct(productRepository.save(product));
        }

        List<Tag> tags = tagRepository.findAllById(request.getTagIds());
        if (tags.size() != request.getTagIds().size()) {
            throw new BadRequestException("Tag not found");
        }

        product.setTags(new LinkedHashSet<>(tags));
        return mapProduct(productRepository.save(product));
    }

    @Override
    @Transactional
    public void bulkAssignCategory(BulkCategoryAssignmentRequest request) {
        Category category = getActiveCategory(request.getCategoryId());
        List<Product> products = productRepository.findAllById(request.getProductIds()).stream()
                .filter(product -> !Boolean.TRUE.equals(product.getDeleted()))
                .toList();

        if (products.size() != request.getProductIds().size()) {
            throw new ResourceNotFoundException("Product not found");
        }

        products.forEach(product -> product.setCategory(category));
        productRepository.saveAll(products);
    }

    private Product getProductEntity(Long id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private Category getActiveCategory(Long categoryId) {
        Category category = categoryRepository.findByIdAndDeletedFalse(categoryId)
                .orElseThrow(() -> new BadRequestException("Category not found"));
        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BadRequestException("Cannot assign to inactive category");
        }
        return category;
    }

    private void validateSku(String sku, Long currentId) {
        String normalizedSku = sku == null ? null : sku.trim();
        if (!StringUtils.hasText(normalizedSku)) {
            throw new BadRequestException("SKU is required");
        }

        boolean exists = currentId == null
                ? productRepository.existsBySkuIgnoreCase(normalizedSku)
                : productRepository.existsBySkuIgnoreCaseAndIdNot(normalizedSku, currentId);
        if (exists) {
            throw new ConflictException("SKU already exists");
        }
    }

    private String generateDuplicateSku(String baseSku) {
        String candidate = baseSku + "-COPY";
        if (!productRepository.existsBySkuIgnoreCase(candidate)) {
            return candidate;
        }

        int sequence = 1;
        while (productRepository.existsBySkuIgnoreCase(candidate + "-" + sequence)) {
            sequence++;
        }
        return candidate + "-" + sequence;
    }

    private Sort.Direction resolveDirection(String order) {
        return "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String resolveSortField(String sort) {
        Map<String, String> allowedSorts = Map.of(
                "price", "price",
                "name", "name",
                "created_at", "createdAt",
                "updated_at", "updatedAt"
        );
        return allowedSorts.getOrDefault(Optional.ofNullable(sort).orElse("updated_at"), "updatedAt");
    }

    private ProductListItemResponse mapProductListItem(Product product) {
        List<ProductImage> images = productImageRepository.findByProduct_IdOrderByDisplayOrderAscIdAsc(product.getId());
        String thumbnailUrl = images.stream()
                .filter(image -> Boolean.TRUE.equals(image.getPrimaryImage()))
                .findFirst()
                .or(() -> images.stream().findFirst())
                .map(ProductImage::getThumbnailUrl)
                .orElse(null);

        Double avgRating = reviewRepository.getAverageRatingByProductId(product.getId());
        Integer revCount = reviewRepository.getReviewCountByProductId(product.getId());

        return ProductListItemResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .price(product.getPrice())
                .available(product.getAvailable())
                .status(product.getStatus())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .thumbnailUrl(thumbnailUrl)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .reviewCount(revCount != null ? revCount : 0)
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductResponse mapProduct(Product product) {
        List<ProductImage> images = productImageRepository.findByProduct_IdOrderByDisplayOrderAscIdAsc(product.getId());
        List<ProductAvailability> availabilities = productAvailabilityRepository.findByProduct_IdOrderByFranchiseIdAsc(product.getId());

        Double avgRating = reviewRepository.getAverageRatingByProductId(product.getId());
        Integer revCount = reviewRepository.getReviewCountByProductId(product.getId());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .available(product.getAvailable())
                .status(product.getStatus())
                .preparationTime(product.getPreparationTime())
                .category(mapCategorySummary(product.getCategory()))
                .tags(product.getTags().stream().map(Tag::getName).sorted().toList())
                .images(images.stream().map(this::mapImage).toList())
                .availability(mapAvailability(product, availabilities))
                .averageRating(avgRating != null ? avgRating : 0.0)
                .reviewCount(revCount != null ? revCount : 0)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private CategoryResponse mapCategorySummary(Category category) {
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
                .subcategories(List.of())
                .build();
    }

    private ProductImageResponse mapImage(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .thumbnailUrl(image.getThumbnailUrl())
                .mediumUrl(image.getMediumUrl())
                .largeUrl(image.getLargeUrl())
                .primary(image.getPrimaryImage())
                .displayOrder(image.getDisplayOrder())
                .createdAt(image.getCreatedAt())
                .build();
    }

    private ProductAvailabilityResponse mapAvailability(Product product, List<ProductAvailability> availabilities) {
        return ProductAvailabilityResponse.builder()
                .productId(product.getId())
                .globalAvailable(product.getAvailable())
                .availableFrom(product.getAvailableFrom())
                .availableUntil(product.getAvailableUntil())
                .autoUnavailableWhenOutOfStock(product.getAutoUnavailableWhenOutOfStock())
                .franchises(availabilities.stream()
                        .map(availability -> ProductAvailabilityItemResponse.builder()
                                .franchiseId(availability.getFranchiseId())
                                .available(availability.getAvailable())
                                .priceOverride(availability.getPriceOverride())
                                .availableFrom(availability.getAvailableFrom())
                                .availableUntil(availability.getAvailableUntil())
                                .build())
                        .toList())
                .build();
    }

    @Override
    @Transactional
    public void removeFromCategory(Long id) {
        Product product = getProductEntity(id);
        if (product == null) {
            throw new BadRequestException("Product no exits");
        }
        product.setCategory(null);
        productRepository.save(product);
    }

    @Override
    public List<CategoryResponse> getAllCategoriesForDropdown() {
        return categoryRepository.findAll().stream()
                .filter(category -> Boolean.TRUE.equals(category.getActive()))
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .active(category.getActive())
                        .build())
                .toList();
    }

    @Override
    public List<TagResponse> getProductTags(Long productId) {
        Product product = getProductEntity(productId);
        return product.getTags().stream()
                .filter(tag -> Boolean.TRUE.equals(tag.getActive()))
                .map(tag -> TagResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .description(tag.getDescription())
                        .active(tag.getActive())
                        .build())
                .toList();
    }

    @Override
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .filter(tag -> Boolean.TRUE.equals(tag.getActive()))
                .map(tag -> TagResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .description(tag.getDescription())
                        .active(tag.getActive())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public TagResponse createTag(TagRequest request) {
        if (tagRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("Tag slug already exists");
        }
        Tag tag = Tag.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        Tag saved = tagRepository.save(tag);
        return TagResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .active(saved.getActive())
                .build();
    }

    @Override
    @Transactional
    public TagResponse updateTag(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        
        if (!tag.getSlug().equals(request.getSlug()) && tagRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("Tag slug already exists");
        }

        tag.setName(request.getName());
        tag.setSlug(request.getSlug());
        tag.setDescription(request.getDescription());
        if (request.getActive() != null) {
            tag.setActive(request.getActive());
        }

        Tag saved = tagRepository.save(tag);
        return TagResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .active(saved.getActive())
                .build();
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        tag.setActive(false);
        tagRepository.save(tag);
    }
}
