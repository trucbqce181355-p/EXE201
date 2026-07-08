package com.group1.production_service.controller;

import com.group1.production_service.dto.request.ProductImageReorderRequest;
import com.group1.production_service.dto.response.ApiResponse;
import com.group1.production_service.dto.response.ProductImageResponse;
import com.group1.production_service.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(path = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<ProductImageResponse>> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {
        ProductImageResponse data = productImageService.uploadImage(id, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Product image uploaded", data));
    }

    @PostMapping(path = "/{id}/images/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> images) {
        List<ProductImageResponse> data = productImageService.uploadImages(id, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Product images uploaded", data));
    }

    @PutMapping("/{id}/images/{imageId}/primary")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> setPrimaryImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Primary image updated", productImageService.setPrimaryImage(id, imageId)));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        productImageService.deleteImage(id, imageId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product image deleted", null));
    }

    @PutMapping("/{id}/images/reorder")
    @PreAuthorize("hasAuthority('PRODUCT:UPDATE')")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> reorderImages(
            @PathVariable Long id,
            @Valid @RequestBody List<ProductImageReorderRequest> requests) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Product images reordered", productImageService.reorderImages(id, requests)));
    }
}
