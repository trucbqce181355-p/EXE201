package com.group1.production_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ProductAvailabilityItemResponse {
    private Long franchiseId;
    private Boolean available;
    private BigDecimal priceOverride;
    private LocalDate availableFrom;
    private LocalDate availableUntil;
}
