package com.group1.customer_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentCriteriaDTO {

    @NotNull(message = "Criteria logic is required")
    private SegmentLogic logic;

    @Min(value = 0, message = "totalSpent must be >= 0")
    private Double totalSpent;
    private String conditionSpent;

    @Min(value = 0, message = "orderCount must be >= 0")
    @Max(value = Integer.MAX_VALUE, message = "orderCount is too large")
    private Integer orderCount;
    private String conditionCount;

    private LocalDate lastOrderDate;
    private String conditionDate;

    private String loyaltyTier;
    private String conditionTier;

    private String location;
    private String conditionLocation;
}

