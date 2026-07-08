package com.group1.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {
    private final String avatarLocation;

    public UploadResourceConfig(@Value("${app.upload.avatar-dir:uploads/avatars}") String avatarDirectory) {
        Path directory = Paths.get(avatarDirectory).toAbsolutePath().normalize();
        this.avatarLocation = directory.toUri().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(avatarLocation);
    }
}
