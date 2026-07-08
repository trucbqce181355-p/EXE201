package com.group1.production_service.service.impl;

import com.group1.production_service.dto.request.ProductImageReorderRequest;
import com.group1.production_service.dto.response.ProductImageResponse;
import com.group1.production_service.entity.Product;
import com.group1.production_service.entity.ProductImage;
import com.group1.production_service.exception.BadRequestException;
import com.group1.production_service.exception.ResourceNotFoundException;
import com.group1.production_service.repository.ProductImageRepository;
import com.group1.production_service.repository.ProductRepository;
import com.group1.production_service.service.ProductImageService;
import com.group1.production_service.service.ProductImageStorageService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductImageStorageService productImageStorageService;

    // ===================== AC 40.1 =====================
    @Override
    @Transactional
    public ProductImageResponse uploadImage(Long productId, MultipartFile image) {
        return uploadImages(productId, List.of(image)).get(0);
    }

    // ===================== AC 40.2 =====================
    @Override
    @Transactional
    public List<ProductImageResponse> uploadImages(Long productId, List<MultipartFile> images) {

        if (images == null || images.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        Product product = getProductEntity(productId);

        long existingCount = productImageRepository.countByProduct_Id(productId);
        if (existingCount + images.size() > 10) {
            throw new BadRequestException("Maximum 10 images allowed");
        }

        List<ProductImage> existingImages =
                productImageRepository.findByProduct_IdOrderByDisplayOrderAscIdAsc(productId);

        boolean hasPrimary = existingImages.stream().anyMatch(ProductImage::getPrimaryImage);
        int nextDisplayOrder = existingImages.size() + 1;

        for (MultipartFile file : images) {

            validateImage(file);

            ProductImageStorageService.StoredImageSet stored =
                    productImageStorageService.store(file);

            boolean shouldBePrimary = !hasPrimary;

            ProductImage productImage = ProductImage.builder()
                    .product(product)
                    .imageUrl(stored.imageUrl())
                    .thumbnailUrl(stored.thumbnailUrl())
                    .mediumUrl(stored.mediumUrl())
                    .largeUrl(stored.largeUrl())
                    .primaryImage(shouldBePrimary)
                    .displayOrder(nextDisplayOrder++)
                    .build();

            productImageRepository.save(productImage);
            hasPrimary = hasPrimary || shouldBePrimary;
        }

        return productImageRepository
                .findByProduct_IdOrderByDisplayOrderAscIdAsc(productId)
                .stream()
                .map(this::mapImage)
                .toList();
    }

    // ===================== AC 40.3 =====================
    @Override
    @Transactional
    public List<ProductImageResponse> setPrimaryImage(Long productId, Long imageId) {

        getProductEntity(productId);

        List<ProductImage> images =
                productImageRepository.findByProduct_IdOrderByDisplayOrderAscIdAsc(productId);

        ProductImage target = images.stream()
                .filter(img -> imageId.equals(img.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

        images.forEach(img -> img.setPrimaryImage(false));
        target.setPrimaryImage(true);

        productImageRepository.saveAll(images);

        return images.stream().map(this::mapImage).toList();
    }

    // ===================== AC 40.4 =====================
    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {

        Product product = getProductEntity(productId);

        ProductImage image = productImageRepository
                .findByIdAndProduct_Id(imageId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

        if (productImageRepository.countByProduct_Id(productId) <= 1
                && Boolean.TRUE.equals(product.getAvailable())) {
            throw new BadRequestException("Cannot delete last image while product is available");
        }

        productImageRepository.delete(image);
        productImageStorageService.deleteIfManaged(image.getImageUrl());

        List<ProductImage> remaining =
                productImageRepository.findByProduct_IdOrderByDisplayOrderAscIdAsc(productId);

        int order = 1;
        for (ProductImage img : remaining) {
            img.setDisplayOrder(order++);
        }

        productImageRepository.saveAll(remaining);
    }

    // ===================== AC 40.5 (FIXED) =====================
    @Override
    @Transactional
    public List<ProductImageResponse> reorderImages(Long productId,
                                                    List<ProductImageReorderRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Reorder list is required");
        }

        getProductEntity(productId);

        List<ProductImage> images =
                productImageRepository.findByProduct_IdOrderByDisplayOrderAscIdAsc(productId);

        
        for (ProductImageReorderRequest req : requests) {
            boolean exists = images.stream()
                    .anyMatch(img -> img.getId().equals(req.getImageId()));

            if (!exists) {
                throw new ResourceNotFoundException("Product image not found");
            }
        }

    
        Set<Integer> orders = new HashSet<>();
        for (ProductImageReorderRequest req : requests) {
            if (req.getDisplayOrder() == null || req.getDisplayOrder() < 1) {
                throw new BadRequestException("Display order must be >= 1");
            }

            if (!orders.add(req.getDisplayOrder())) {
                throw new BadRequestException("Duplicate display order");
            }
        }

        Map<Long, Integer> reorderMap = requests.stream()
                .collect(Collectors.toMap(
                        ProductImageReorderRequest::getImageId,
                        ProductImageReorderRequest::getDisplayOrder
                ));

        for (ProductImage image : images) {
            if (reorderMap.containsKey(image.getId())) {
                image.setDisplayOrder(reorderMap.get(image.getId()));
            }
        }

        productImageRepository.saveAll(images);

        return productImageRepository
                .findByProduct_IdOrderByDisplayOrderAscIdAsc(productId)
                .stream()
                .map(this::mapImage)
                .toList();
    }

    // ===================== VALIDATE =====================
    private void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }

        String type = file.getContentType();

        if (type == null ||
                !(type.equals("image/jpeg")
                        || type.equals("image/png")
                        || type.equals("image/webp"))) {
            throw new BadRequestException("Unsupported file format");
        }
    }

    // ===================== HELPER =====================
    private Product getProductEntity(Long productId) {
        return productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
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
}
