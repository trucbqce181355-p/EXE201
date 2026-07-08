package com.group1.production_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkAvailabilityUpdateRequest {

    @NotEmpty(message = "Product ids are required")
    private List<Long> productIds;

    @NotNull(message = "Availability flag is required")
    private Boolean isAvailable;
}
