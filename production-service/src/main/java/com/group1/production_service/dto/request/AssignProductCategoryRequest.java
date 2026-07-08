package com.group1.production_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignProductCategoryRequest {

    @NotNull(message = "Category is required")
    private Long categoryId;
}
