package com.group1.production_service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductAvailabilityRequest {
    private Boolean isAvailable;
    private LocalDate availableFrom;
    private LocalDate availableUntil;
    private Boolean autoUnavailableWhenOutOfStock;
}
