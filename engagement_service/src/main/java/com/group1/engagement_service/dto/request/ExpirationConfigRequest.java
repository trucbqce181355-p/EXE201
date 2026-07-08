package com.group1.engagement_service.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExpirationConfigRequest {

    @NotNull(message = "Expiration months is required")
    @Min(value = 1, message = "expiration_months must be greater than or equal to 1")
    private Integer expirationMonths;

    @NotNull(message = "Evaluation period months is required")
    @Min(value = 1, message = "evaluation_period_months must be greater than or equal to 1")
    private Integer evaluationPeriodMonths;
}
