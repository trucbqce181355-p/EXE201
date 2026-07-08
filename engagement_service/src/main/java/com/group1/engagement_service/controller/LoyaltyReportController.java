package com.group1.engagement_service.controller;

import com.group1.engagement_service.dto.request.PointsActivityReportRequest;
import com.group1.engagement_service.dto.response.*;
import com.group1.engagement_service.service.LoyaltyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loyalty/reports")
@RequiredArgsConstructor
public class LoyaltyReportController {

    private final LoyaltyReportService service;

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('LOYALTY:REPORT')")
    public LoyaltyOverviewResponse overview() {
        return service.getOverview();
    }

    @GetMapping("/tier-distribution")
    @PreAuthorize("hasAuthority('LOYALTY:REPORT')")
    public List<TierDistributionResponse> tierDistribution() {
        return service.getTierDistribution();
    }

    @PostMapping("/points-activity")
    @PreAuthorize("hasAuthority('LOYALTY:REPORT')")
    public List<PointsActivityResponse> pointsActivity(
            @RequestBody PointsActivityReportRequest request) {
        return service.getPointsActivity(request);
    }

    @GetMapping("/engagement")
    @PreAuthorize("hasAuthority('LOYALTY:REPORT')")
    public CustomerEngagementResponse engagement() {
        return service.getCustomerEngagement();
    }
}