package com.group1.production_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProductAvailabilityResponse {
    private Long productId;
    private Boolean globalAvailable;
    private LocalDate availableFrom;
    private LocalDate availableUntil;
    private Boolean autoUnavailableWhenOutOfStock;
    private List<ProductAvailabilityItemResponse> franchises;
}
