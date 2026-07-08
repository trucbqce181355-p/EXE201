package com.group1.engagement_service.dto.loyalty;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TierCompareResponse {
	private List<TierResponse> tiers;
}

