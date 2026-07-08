package com.group1.engagement_service.dto.loyalty;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TierResponse {
	private String name;
	private Integer minPoints;
	private Integer maxPoints;
	private String iconUrl;
	private List<TierBenefitDTO> benefits;
}

