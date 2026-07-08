package com.group1.customer_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileOrderResponse {
    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("order_number")
    private String orderNumber;

    private String status;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("items_count")
    private int itemsCount;
}
