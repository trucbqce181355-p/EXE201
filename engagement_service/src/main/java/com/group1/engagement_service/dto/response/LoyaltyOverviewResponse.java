package com.group1.engagement_service.dto.response;

import lombok.Data;

@Data
public class LoyaltyOverviewResponse {
    private long totalMembers;
    private long activeMembers;
    private long totalPointsIssued;
    private long totalPointsRedeemed;
    private long totalPointsExpired;
}