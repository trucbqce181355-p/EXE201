package com.group1.production_service.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 150, message = "Category name must be <= 150 characters")
    private String name;

    @NotBlank(message = "Category slug is required")
    @Size(max = 180, message = "Category slug must be <= 180 characters")
    private String slug;

    @Size(max = 1000, message = "Description must be <= 1000 characters")
    private String description;

    @Size(max = 500, message = "Image URL must be <= 500 characters")
    private String imageUrl;

    private Long parentId;

    private Integer displayOrder;

    private Boolean active;
}
