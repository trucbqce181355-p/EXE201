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
public class TargetSegmentsResponse {
    private PromotionTargetMode mode;
    private List<Long> segment_ids;
    private List<SegmentTargetInfoDto> segments;
}
