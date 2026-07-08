package com.group1.engagement_service.controller;

import com.group1.engagement_service.dto.request.ExportRequest;
import com.group1.engagement_service.dto.request.PointsActivityReportRequest;
import com.group1.engagement_service.dto.response.CustomerEngagementResponse;
import com.group1.engagement_service.dto.response.LoyaltyOverviewResponse;
import com.group1.engagement_service.dto.response.PromotionAnalyticsResponse;
import com.group1.engagement_service.service.PromotionAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionAnalyticsController {

    private final PromotionAnalyticsService analyticsService;

    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasAuthority('PROMOTION:REPORT')")
    public ResponseEntity<PromotionAnalyticsResponse> getAnalytics(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getPromotionAnalytics(id));
    }

    @GetMapping("/{id}/analytics/usage")
    @PreAuthorize("hasAuthority('PROMOTION:REPORT')")
    public ResponseEntity<?> getUsageOverTime(@PathVariable Long id, @RequestParam String group_by) {
        return ResponseEntity.ok(analyticsService.getUsageHistory(id));
    }

    @GetMapping("/analytics/compare")
    @PreAuthorize("hasAuthority('PROMOTION:REPORT')")
    public ResponseEntity<?> compare(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(analyticsService.comparePromotions(ids));
    }

    // Placeholder cho Export (Thường dùng thư viện Apache POI cho Excel)
    @GetMapping("/{id}/analytics/export")
    @PreAuthorize("hasAuthority('PROMOTION:REPORT')")
    public void exportData(@PathVariable Long id, @RequestParam String format, HttpServletResponse response) {
        // Logic export CSV/Excel ở đây
    }
}
