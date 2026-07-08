package com.group1.engagement_service.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TierDTO {
    private String name;
    private Integer minPoints;
    private Integer maxPoints;
    private String iconUrl;
    private List<TierBenefitDTO> benefits;

    public TierDTO(String name, Integer minPoints, Integer maxPoints, String iconUrl, List<TierBenefitDTO> benefits) {
        this.name = name;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.iconUrl = iconUrl;
        this.benefits = benefits;
    }

    // getters & setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getMinPoints() { return minPoints; }
    public void setMinPoints(Integer minPoints) { this.minPoints = minPoints; }
    public Integer getMaxPoints() { return maxPoints; }
    public void setMaxPoints(Integer maxPoints) { this.maxPoints = maxPoints; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public List<TierBenefitDTO> getBenefits() { return benefits; }
    public void setBenefits(List<TierBenefitDTO> benefits) { this.benefits = benefits; }
}