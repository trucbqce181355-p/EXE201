package com.group1.customer_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLookupResponse {
    private Long customerId;
    private String customerCode;
    private Long userId;
}
