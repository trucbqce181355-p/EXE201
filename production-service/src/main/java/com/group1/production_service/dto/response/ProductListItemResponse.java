package com.group1.production_service.dto.response;

import com.group1.production_service.entity.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductListItemResponse {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private Boolean available;
    private ProductStatus status;
    private Long categoryId;
    private String categoryName;
    private String thumbnailUrl;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime updatedAt;
}
