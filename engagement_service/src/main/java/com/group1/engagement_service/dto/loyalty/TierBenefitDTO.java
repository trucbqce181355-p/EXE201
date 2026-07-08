package com.group1.engagement_service.dto.loyalty;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TierBenefitDTO {
	private String type;
	private BigDecimal value;
	private String description;
}

