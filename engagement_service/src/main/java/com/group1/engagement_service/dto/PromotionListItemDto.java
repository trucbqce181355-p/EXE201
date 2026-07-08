package com.group1.engagement_service.dto;

import com.group1.engagement_service.entity.PromotionStatus;
import com.group1.engagement_service.entity.PromotionTargetMode;
import com.group1.engagement_service.entity.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionListItemDto {
    private Long id;
    private String name;
    private PromotionStatus status;
    private PromotionType type;
    private BigDecimal value;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String targetSegmentIds;
    private PromotionTargetMode targetSegmentMode;
}
