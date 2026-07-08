package com.group1.engagement_service.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierHistoryDTO {

    private String fromTier;
    private String toTier;

    private LocalDateTime changedAt;
}