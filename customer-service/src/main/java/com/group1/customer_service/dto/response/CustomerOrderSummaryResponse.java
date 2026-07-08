package com.group1.customer_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CustomerOrderSummaryResponse {
    private Long orderId;
    private String orderCode;
    private String orderType;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
