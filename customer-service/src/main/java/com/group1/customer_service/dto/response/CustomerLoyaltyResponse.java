package com.group1.customer_service.dto.response;

import com.group1.customer_service.entity.CustomerSegment;
import com.group1.customer_service.entity.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CustomerLoyaltyResponse {
    private Long id;
    private String customerCode;
    private LoyaltyTier loyaltyTier;
    private Integer loyaltyPoints;
    private CustomerSegment segment;
    private LocalDateTime updatedAt;
}
