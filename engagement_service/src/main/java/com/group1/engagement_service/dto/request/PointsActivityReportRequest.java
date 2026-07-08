package com.group1.engagement_service.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PointsActivityReportRequest {
    private LocalDate from;
    private LocalDate to;
    private String groupBy; // day | week | month
}