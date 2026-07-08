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
public class CustomerProfileSummaryResponse {
    @JsonProperty("total_orders")
    private long totalOrders;

    @JsonProperty("total_spent")
    private BigDecimal totalSpent;

    @JsonProperty("average_order_value")
    private BigDecimal averageOrderValue;

    @JsonProperty("last_order_date")
    private LocalDateTime lastOrderDate;
}
