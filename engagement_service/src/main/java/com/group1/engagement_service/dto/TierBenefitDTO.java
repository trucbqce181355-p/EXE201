package com.group1.engagement_service.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TierBenefitDTO {

    private String description;
    private String type;
    private BigDecimal value;

    public TierBenefitDTO(String description, String type, BigDecimal value) {
        this.description = description;
        this.type = type;
        this.value = value;
    }

   
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
}