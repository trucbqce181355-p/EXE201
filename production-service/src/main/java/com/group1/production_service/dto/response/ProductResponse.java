package com.group1.production_service.dto.response;

import com.group1.production_service.entity.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private Boolean available;
    private ProductStatus status;
    private Integer preparationTime;
    private CategoryResponse category;
    private List<String> tags;
    private List<ProductImageResponse> images;
    private ProductAvailabilityResponse availability;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
