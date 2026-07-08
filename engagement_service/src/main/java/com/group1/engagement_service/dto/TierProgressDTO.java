/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.group1.engagement_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TierProgressDTO {

    private String currentTier;
    private String nextTier;

    private Integer currentPoints;
    private Integer pointsToNextTier;

    private Integer currentThreshold;
    private Integer nextThreshold;

    private Integer progressPercentage;
}