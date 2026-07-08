package com.group1.customer_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyResponseDTO {

    private boolean enrolled;

    @JsonProperty("enrolled_at")
    private LocalDateTime enrolledAt;

    @JsonProperty("current_tier")
    private TierDTO currentTier;

    @JsonProperty("current_points")
    private Integer currentPoints;

    @JsonProperty("lifetime_points")
    private Integer lifetimePoints;

    @JsonProperty("tier_progress")
    private TierProgressDTO tierProgress;

    @JsonProperty("points_expiring_soon")
    private PointsExpiringDTO pointsExpiringSoon;
}