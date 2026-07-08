package com.group1.engagement_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidateCouponResponse(
        boolean valid,
        String message,
        @JsonProperty("promotion_id") Long promotionId,
        @JsonProperty("coupon_id") Long couponId
) {
}
