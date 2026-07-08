package com.group1.engagement_service.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ValidateCouponRequest {

    private String code;

    @JsonProperty("customer_id")
    private Long customerId;
}
