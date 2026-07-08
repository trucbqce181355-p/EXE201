package com.group1.customer_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileLoyaltyResponse {
    @JsonProperty("current_tier")
    private String currentTier;

    @JsonProperty("current_points")
    private Integer currentPoints;
}
