package com.group1.engagement_service.dto.response;

import lombok.Data;

@Data
public class CustomerEngagementResponse {
    private double avgPointsPerCustomer;
    private double redemptionRate;
    private double tierUpgradeRate;
    private double tierDowngradeRate;
}