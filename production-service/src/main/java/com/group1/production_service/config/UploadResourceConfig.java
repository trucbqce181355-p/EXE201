package com.group1.production_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    private final String productImageLocation;

    public UploadResourceConfig(@Value("${app.upload.product-image-dir:uploads/products}") String productImageDirectory) {
        Path directory = Paths.get(productImageDirectory).toAbsolutePath().normalize();
        this.productImageLocation = directory.toUri().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(productImageLocation);
    }
}
