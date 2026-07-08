package com.group1.engagement_service.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Subset of customer-service {@code SegmentResponse} JSON.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerSegmentDetailDto {
    private Long id;
    private String name;
    private String description;
    private Long customerCount;
}
