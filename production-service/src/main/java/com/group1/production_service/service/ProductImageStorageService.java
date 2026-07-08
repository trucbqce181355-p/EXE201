package com.group1.production_service.service;

import com.group1.production_service.exception.BadRequestException;
import com.group1.production_service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductImageStorageService {

    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path imageDirectory;
    private final String publicBaseUrl;

    public ProductImageStorageService(
            @Value("${app.upload.product-image-dir:uploads/products}") String imageDirectory,
            @Value("${app.public.base-url:http://localhost:8084}") String publicBaseUrl) {
        this.imageDirectory = Paths.get(imageDirectory).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;
    }

    public StoredImageSet store(MultipartFile imageFile) {
        validateImage(imageFile);

        String extension = resolveExtension(imageFile);
        String fileName = UUID.randomUUID() + extension;
        Path targetFile = imageDirectory.resolve(fileName).normalize();

        try {
            Files.createDirectories(imageDirectory);
            imageFile.transferTo(targetFile);
        } catch (IOException ex) {
            throw new ResourceNotFoundException("Unable to store product image");
        }

        String publicUrl = publicBaseUrl + "/uploads/products/" + fileName;

        // Skeleton placeholder: wire real resize/optimization output here later.
        return new StoredImageSet(publicUrl, publicUrl, publicUrl, publicUrl);
    }

    public void deleteIfManaged(String publicUrl) {
        if (!StringUtils.hasText(publicUrl)) {
            return;
        }

        String prefix = publicBaseUrl + "/uploads/products/";
        if (!publicUrl.startsWith(prefix)) {
            return;
        }

        String fileName = publicUrl.substring(prefix.length());
        Path targetFile = imageDirectory.resolve(fileName).normalize();
        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException ignored) {
            // Best-effort cleanup for skeleton storage layer.
        }
    }

    private void validateImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }
        if (imageFile.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }

        String contentType = imageFile.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Unsupported file format");
        }
    }

    private String resolveExtension(MultipartFile imageFile) {
        String originalFilename = imageFile.getOriginalFilename();
        if (StringUtils.hasText(originalFilename)) {
            int extensionIndex = originalFilename.lastIndexOf('.');
            if (extensionIndex >= 0 && extensionIndex < originalFilename.length() - 1) {
                String extension = originalFilename.substring(extensionIndex).toLowerCase();
                if (Set.of(".jpg", ".jpeg", ".png", ".webp").contains(extension)) {
                    return ".jpeg".equals(extension) ? ".jpg" : extension;
                }
            }
        }

        String contentType = imageFile.getContentType();
        String derivedExtension = CONTENT_TYPE_TO_EXTENSION.get(contentType);
        if (derivedExtension != null) {
            return derivedExtension;
        }

        throw new BadRequestException("Unsupported file format");
    }

    public record StoredImageSet(
            String imageUrl,
            String thumbnailUrl,
            String mediumUrl,
            String largeUrl
    ) {
    }
}
