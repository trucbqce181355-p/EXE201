package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.request.PointsActivityReportRequest;
import com.group1.engagement_service.dto.response.*;
import com.group1.engagement_service.entity.PointHistory;
import com.group1.engagement_service.repository.LoyaltyBalanceRepository;
import com.group1.engagement_service.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LoyaltyReportService {

    private final LoyaltyBalanceRepository loyaltyRepo;
    private final PointHistoryRepository pointHistoryRepo;

    public LoyaltyOverviewResponse getOverview() {
        LoyaltyOverviewResponse res = new LoyaltyOverviewResponse();
        res.setTotalMembers(loyaltyRepo.countTotalMembers());
        res.setActiveMembers(loyaltyRepo.countActiveMembers());

        // Đảm bảo không bị Null nếu Repository trả về giá trị trống
        res.setTotalPointsIssued(pointHistoryRepo.totalPointsIssued());
        res.setTotalPointsRedeemed(pointHistoryRepo.totalPointsRedeemed());
        res.setTotalPointsExpired(0);
        return res;
    }

    public List<PointsActivityResponse> getPointsActivity(PointsActivityReportRequest req) {
        LocalDateTime from = req.getFrom().atStartOfDay();
        LocalDateTime to = req.getTo().atTime(23, 59, 59);

        List<PointHistory> histories = pointHistoryRepo.findByCreatedAtBetween(from, to);
        // Dùng TreeMap nếu muốn các ngày hiển thị theo thứ tự thời gian tăng dần
        Map<String, Map<String, Long>> map = new TreeMap<>();

        for (PointHistory ph : histories) {
            String period = ph.getCreatedAt().toLocalDate().toString();
            map.putIfAbsent(period, new HashMap<>());

            // Xử lý an toàn nếu ph.getAmount() bị null
            long amount = (ph.getAmount() != null) ? ph.getAmount().longValue() : 0L;
            String type = (ph.getType() != null) ? ph.getType() : "UNKNOWN";

            map.get(period).merge(type, amount, Long::sum);
        }

        List<PointsActivityResponse> result = new ArrayList<>();
        map.forEach((period, typeMap) -> {
            typeMap.forEach((type, total) -> {
                PointsActivityResponse r = new PointsActivityResponse();
                r.setPeriod(period);
                r.setType(type);
                r.setTotalPoints(total);
                result.add(r);
            });
        });
        return result;
    }

    public List<TierDistributionResponse> getTierDistribution() {
        List<Object[]> stats = loyaltyRepo.countMembersByTier();
        List<TierDistributionResponse> result = new ArrayList<>();

        for (Object[] row : stats) {
            TierDistributionResponse r = new TierDistributionResponse();
            // row[0] là Name, row[1] là Count
            r.setTierName(row[0] != null ? row[0].toString() : "No Tier");
            r.setMemberCount(row[1] != null ? ((Number) row[1]).longValue() : 0L);
            result.add(r);
        }
        return result;
    }

    public CustomerEngagementResponse getCustomerEngagement() {
        CustomerEngagementResponse res = new CustomerEngagementResponse();

        long totalMembers = loyaltyRepo.countTotalMembers();
        long issued = pointHistoryRepo.totalPointsIssued();
        long redeemed = pointHistoryRepo.totalPointsRedeemed();

        // Ép kiểu double để tránh phép chia số nguyên (ví dụ 1/2 = 0.0)
        res.setAvgPointsPerCustomer(totalMembers == 0 ? 0.0 : (double) issued / totalMembers);
        res.setRedemptionRate(issued == 0 ? 0.0 : (double) redeemed / issued);

        res.setTierUpgradeRate(0.0);
        res.setTierDowngradeRate(0.0);
        return res;
    }
}