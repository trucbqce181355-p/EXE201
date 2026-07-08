package com.group1.customer_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PointsHistoryDTO {

    private String type;          // EARNED / REDEEMED / EXPIRED / ADJUSTED / BONUS

    private Integer amount;

    private String description;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}