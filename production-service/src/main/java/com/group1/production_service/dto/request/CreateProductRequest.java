package com.group1.production_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 180, message = "Product name must be <= 180 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 80, message = "SKU must be <= 80 characters")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @Size(max = 2000, message = "Description must be <= 2000 characters")
    private String description;

    private Boolean available;

    private Integer preparationTime;
}
