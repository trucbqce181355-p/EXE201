package com.group1.engagement_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.group1.engagement_service.entity.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Public promotion card — dùng cho /available và /featured.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromotionPublicResponse {

    private String name;
    private String description;
    private PromotionType type;
    private BigDecimal value;
    private String conditions;

    @JsonProperty("end_date")
    private LocalDateTime endDate;

    @JsonProperty("image_url")
    private String imageUrl;
}
