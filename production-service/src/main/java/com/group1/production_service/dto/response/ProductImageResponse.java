package com.group1.production_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    private String thumbnailUrl;
    private String mediumUrl;
    private String largeUrl;
    private Boolean primary;
    private Integer displayOrder;
    private LocalDateTime createdAt;
}
