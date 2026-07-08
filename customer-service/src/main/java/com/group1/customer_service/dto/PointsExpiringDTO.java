package com.group1.customer_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PointsExpiringDTO {

    private int amount;

    @JsonProperty("expiry_date")
    private LocalDate expiryDate;
}