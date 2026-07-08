package com.group1.production_service.service;

import com.group1.production_service.dto.request.ProductImageReorderRequest;
import com.group1.production_service.dto.response.ProductImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {

    ProductImageResponse uploadImage(Long productId, MultipartFile image);

    List<ProductImageResponse> uploadImages(Long productId, List<MultipartFile> images);

    List<ProductImageResponse> setPrimaryImage(Long productId, Long imageId);

    void deleteImage(Long productId, Long imageId);

    List<ProductImageResponse> reorderImages(Long productId, List<ProductImageReorderRequest> requests);
}