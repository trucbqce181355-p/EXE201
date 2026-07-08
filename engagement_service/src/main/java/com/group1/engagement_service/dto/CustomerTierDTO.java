package com.group1.engagement_service.dto;

import java.util.List;

public class CustomerTierDTO {
    private String name;
    private int minPoints;
    private Integer maxPoints;
    private String iconUrl;
    private List<TierBenefitDTO> benefits;

    private int progressPercentage;
    private int pointsToNextTier;
    private String nextTierName;
    private boolean isMaxTier;

    public CustomerTierDTO(String name, int minPoints, Integer maxPoints, String iconUrl,
                           List<TierBenefitDTO> benefits,
                           int progressPercentage,
                           int pointsToNextTier,
                           String nextTierName,
                           boolean isMaxTier) {
        this.name = name;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.iconUrl = iconUrl;
        this.benefits = benefits;
        this.progressPercentage = progressPercentage;
        this.pointsToNextTier = pointsToNextTier;
        this.nextTierName = nextTierName;
        this.isMaxTier = isMaxTier;
    }

    // getters/setters
    public String getName() { return name; }
    public int getMinPoints() { return minPoints; }
    public Integer getMaxPoints() { return maxPoints; }
    public String getIconUrl() { return iconUrl; }
    public List<TierBenefitDTO> getBenefits() { return benefits; }

    public int getProgressPercentage() { return progressPercentage; }
    public int getPointsToNextTier() { return pointsToNextTier; }
    public String getNextTierName() { return nextTierName; }
    public boolean isMaxTier() { return isMaxTier; }
}