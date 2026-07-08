package com.group1.engagement_service.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PointConfigRequest {

    @NotNull(message = "Points per currency is required")
    @Positive(message = "points_per_currency must be greater than 0")
    private BigDecimal pointsPerCurrency;

    @NotNull(message = "Minimum order amount is required")
    @PositiveOrZero(message = "Minimum order amount must be greater than or equal to 0")
    private BigDecimal minOrderAmount;

    private List<String> excludedCategories;
}
