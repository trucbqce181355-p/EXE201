package com.group1.engagement_service.dto.loyalty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerTierResponse {
	private String currentTier;
	private ListBenefit benefits;
	private Progress progress;
	private boolean isMaxTier;

	@Data
	@Builder
	public static class ListBenefit {
		private java.util.List<TierBenefitDTO> items;
	}

	@Data
	@Builder
	public static class Progress {
		private Integer pointsToNextTier;
		private String nextTierName;
		private Double progressPercentage;
	}
}

