package com.group1.customer_service.dto.response;

import com.group1.customer_service.entity.CustomerSegment;
import com.group1.customer_service.entity.CustomerStatus;
import com.group1.customer_service.entity.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private Long authUserId;
    private String customerCode;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private CustomerStatus status;
    private CustomerSegment segment;
    private LoyaltyTier loyaltyTier;
    private Integer loyaltyPoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
