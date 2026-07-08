package com.group1.production_service.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CategoryReorderRequest {

    @NotNull(message = "Category id is required")
    private Long id;

    @NotNull(message = "Display order is required")
    private Integer displayOrder;
}
