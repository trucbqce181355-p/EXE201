package com.group1.engagement_service.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class LoyaltyConfigResponse {
    private BigDecimal pointsPerCurrency;
    private BigDecimal minOrderAmount;
    private List<String> excludedCategories;
    private Integer expirationMonths;
    private Integer evaluationPeriodMonths;
    private Boolean inheritFromLowerTiers;
    private List<TierConfigResponse> tiers;
}
