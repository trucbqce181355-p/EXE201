package com.group1.engagement_service.dto.response;

import lombok.Data;

@Data
public class PointsActivityResponse {
    private String period;
    private String type; // EARN | REDEEM | ADJUSTMENT | EXPIRED
    private long totalPoints;
}