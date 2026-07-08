package com.group1.engagement_service.dto;

import com.group1.engagement_service.entity.PromotionTargetMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewReachResponse {
    private Long total_customers;
    private PromotionTargetMode mode;
    private List<SegmentTargetInfoDto> segments;
    private String counting_note;
}
