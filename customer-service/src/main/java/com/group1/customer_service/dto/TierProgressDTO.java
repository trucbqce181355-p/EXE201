package com.group1.customer_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TierProgressDTO {

    private String currentTier;
    private String nextTier;

    private Integer currentPoints;       // ✅ thêm
    private Integer pointsToNextTier;

    private Integer currentThreshold;    // ✅ thêm
    private Integer nextThreshold;       // ✅ thêm

    private Integer progressPercentage;
}