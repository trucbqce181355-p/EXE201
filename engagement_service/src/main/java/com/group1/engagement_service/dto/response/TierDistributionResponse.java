package com.group1.engagement_service.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierDistributionResponse {
    private String tierName;
    private Long memberCount;
}