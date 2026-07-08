package com.group1.production_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkCategoryAssignmentRequest {

    @NotEmpty(message = "Product ids are required")
    private List<Long> productIds;

    @NotNull(message = "Category is required")
    private Long categoryId;
}
