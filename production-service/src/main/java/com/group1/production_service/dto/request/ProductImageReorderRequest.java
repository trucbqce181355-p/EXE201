package com.group1.production_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductImageReorderRequest {

    @NotNull(message = "Image id is required")
    private Long imageId;

    @NotNull(message = "Display order is required")
    private Integer displayOrder;
}
