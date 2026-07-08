package com.group1.customer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSimpleDTO {
    private Long customerId;
    private String customerCode;
    private Long userId;
    private String fullName;
    private String email;
}

