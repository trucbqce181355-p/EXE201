package com.group1.production_service.dto.request;

import com.group1.production_service.entity.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {

    @Size(max = 180, message = "Product name must be <= 180 characters")
    private String name;

    @Size(max = 80, message = "SKU must be <= 80 characters")
    private String sku;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal price;

    private Long categoryId;

    @Size(max = 2000, message = "Description must be <= 2000 characters")
    private String description;

    private Boolean available;

    private Integer preparationTime;

    private ProductStatus status;
}
