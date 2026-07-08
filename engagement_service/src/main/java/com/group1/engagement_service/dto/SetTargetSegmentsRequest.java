package com.group1.engagement_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.group1.engagement_service.entity.PromotionTargetMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SetTargetSegmentsRequest {

    /**
     * Customer-segment IDs (JSON key {@code segment_ids}).
     */
    @NotEmpty
    @JsonProperty("segment_ids")
    private List<String> segmentIds;

    @NotNull
    private PromotionTargetMode mode;
}
