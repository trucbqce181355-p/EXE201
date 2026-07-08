package com.group1.engagement_service.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionAnalyticsResponse {
    private Long promotionId;
    private long totalUses;
    private BigDecimal totalDiscountGiven;
    private BigDecimal totalRevenueGenerated;
    private double conversionRate;
    private double roi;
    private CustomerMetrics customerMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerMetrics {
        private long uniqueCustomers;
    }
}