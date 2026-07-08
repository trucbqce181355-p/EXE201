package com.group1.engagement_service.service;

import com.group1.engagement_service.entity.LoyaltyBalance;
import com.group1.engagement_service.entity.LoyaltyRedemption;
import com.group1.engagement_service.entity.PointHistory;
import com.group1.engagement_service.entity.RedemptionStatus;
import com.group1.engagement_service.entity.Reward;
import com.group1.engagement_service.repository.LoyaltyBalanceRepository;
import com.group1.engagement_service.repository.LoyaltyRedemptionRepository;
import com.group1.engagement_service.repository.PointHistoryRepository;
import com.group1.engagement_service.repository.RewardRepository;
import com.group1.engagement_service.request.RedeemDiscountRequest;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.client.RestTemplate;

@Service
public class RewardService {

    private final RewardRepository rewardRepository;
    private final LoyaltyBalanceRepository loyaltyBalanceRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final LoyaltyRedemptionRepository loyaltyRedemptionRepository;

    public RewardService(RewardRepository rewardRepository, LoyaltyBalanceRepository loyaltyBalanceRepository, PointHistoryRepository pointHistoryRepository, LoyaltyRedemptionRepository loyaltyRedemptionRepository) {
        this.rewardRepository = rewardRepository;
        this.loyaltyBalanceRepository = loyaltyBalanceRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.loyaltyRedemptionRepository = loyaltyRedemptionRepository;
    }

    // ✅ AC 34.1
    public List<Reward> getAvailableRewards() {
        return rewardRepository.findByAvailabilityTrue();
    }

    // ✅ AC 34.2 - Redeem Reward
    public String redeemReward(Long customerId, Long rewardId, int points) {

        // 1. Check reward tồn tại
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Reward not found"));

        // 2. Check available
        if (!reward.isAvailability()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Reward not available");
        }

        // 3. Lấy balance
        LoyaltyBalance balance = loyaltyBalanceRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Loyalty balance not found"));

        // 4. Check đủ điểm
        if (balance.getCurrentPoints() < points) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Insufficient points");
        }

        // 5. Check đủ đổi reward
        if (points < reward.getRequiredPoints()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Not enough points for this reward");
        }

        // 6. Trừ điểm
        balance.setCurrentPoints(balance.getCurrentPoints() - points);
        loyaltyBalanceRepository.save(balance);

        // 7. Ghi history
        PointHistory history = new PointHistory();
        history.setCustomerId(customerId);
        history.setAmount(-points);
        history.setType("REDEEM");
        history.setReason("Redeem reward ID " + rewardId);
        history.setCreatedAt(LocalDateTime.now());

        pointHistoryRepository.save(history);

        return "Redeem reward success";
    }

    

    // ✅ AC 34.5 - Redemption History
    public List<PointHistory> getRedemptionHistory(Long customerId) {

        List<PointHistory> histories
                = pointHistoryRepository.findByCustomerIdAndTypeOrderByCreatedAtDesc(
                        customerId, "REDEEM"
                );

        if (histories.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No redemption history found"
            );
        }

        return histories;
    }

    
}
