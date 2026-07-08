package com.group1.engagement_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SegmentTargetInfoDto {
    private Long segment_id;
    private String name;
    private String description;
    private Long customer_count;
}
