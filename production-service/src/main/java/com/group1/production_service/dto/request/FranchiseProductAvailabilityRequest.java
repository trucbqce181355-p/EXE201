package com.group1.production_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FranchiseProductAvailabilityRequest {
    private Boolean isAvailable;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price override must be positive")
    private BigDecimal priceOverride;

    private LocalDate availableFrom;
    private LocalDate availableUntil;
}
