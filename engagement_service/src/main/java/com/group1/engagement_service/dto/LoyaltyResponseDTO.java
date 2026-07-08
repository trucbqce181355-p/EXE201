/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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