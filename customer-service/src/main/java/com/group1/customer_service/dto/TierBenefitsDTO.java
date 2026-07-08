package com.group1.customer_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TierBenefitsDTO {

    @JsonProperty("discount_percentage")
    private int discountPercentage;

    @JsonProperty("free_shipping_threshold")
    private int freeShippingThreshold;

    @JsonProperty("birthday_bonus_points")
    private int birthdayBonusPoints;

    @JsonProperty("exclusive_promotions")
    private boolean exclusivePromotions;

    @JsonProperty("priority_support")
    private boolean prioritySupport; 
}