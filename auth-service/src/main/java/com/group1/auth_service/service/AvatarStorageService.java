package com.group1.auth_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AvatarStorageService {
    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );
    private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    private final Path avatarDirectory;
    private final String publicBaseUrl;

    public AvatarStorageService(
            @Value("${app.upload.avatar-dir:uploads/avatars}") String avatarDirectory,
            @Value("${app.public.base-url:http://localhost:8081}") String publicBaseUrl) {
        this.avatarDirectory = Paths.get(avatarDirectory).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;
    }

    public String storeAvatar(MultipartFile avatarFile) {
        validateAvatarFile(avatarFile);

        String extension = resolveExtension(avatarFile);
        String fileName = UUID.randomUUID() + extension;
        Path targetFile = avatarDirectory.resolve(fileName).normalize();

        try {
            Files.createDirectories(avatarDirectory);
            avatarFile.transferTo(targetFile);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store avatar image");
        }

        return publicBaseUrl + "/uploads/avatars/" + fileName;
    }

    private void validateAvatarFile(MultipartFile avatarFile) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar image is required");
        }
        if (avatarFile.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar image must be 5 MB or smaller");
        }

        String contentType = avatarFile.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Avatar image must be a JPG, PNG, GIF, or WEBP file"
            );
        }
    }

    private String resolveExtension(MultipartFile avatarFile) {
        String originalFilename = avatarFile.getOriginalFilename();
        if (StringUtils.hasText(originalFilename)) {
            int extensionIndex = originalFilename.lastIndexOf('.');
            if (extensionIndex >= 0 && extensionIndex < originalFilename.length() - 1) {
                String extension = originalFilename.substring(extensionIndex).toLowerCase();
                if (Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(extension)) {
                    return ".jpeg".equals(extension) ? ".jpg" : extension;
                }
            }
        }

        String contentType = avatarFile.getContentType();
        String derivedExtension = CONTENT_TYPE_TO_EXTENSION.get(contentType);
        if (derivedExtension != null) {
            return derivedExtension;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Avatar image must be a JPG, PNG, GIF, or WEBP file"
        );
    }
}
