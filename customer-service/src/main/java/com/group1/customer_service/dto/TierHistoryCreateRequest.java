package com.group1.customer_service.dto;

import lombok.Data;

@Data
public class TierHistoryCreateRequest {
    private String fromTier;
    private String toTier;
    private String reason; // enum name of TierChangeReason
}

