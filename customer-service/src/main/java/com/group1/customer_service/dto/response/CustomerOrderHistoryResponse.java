package com.group1.customer_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CustomerOrderHistoryResponse {
    private Long customerId;
    private String customerCode;
    private String customerName;
    private int totalOrders;
    private List<CustomerOrderSummaryResponse> orders;
}
