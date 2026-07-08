package com.group1.engagement_service.service;

import com.group1.engagement_service.client.CustomerSegmentFeignClient;
import com.group1.engagement_service.dto.LoyaltyResponseDTO;
import com.group1.engagement_service.dto.PointsHistoryDTO;
import com.group1.engagement_service.dto.TierBenefitDTO;
import com.group1.engagement_service.dto.TierDTO;
import com.group1.engagement_service.dto.TierHistoryDTO;
import com.group1.engagement_service.dto.TierProgressDTO;
import com.group1.engagement_service.dto.loyalty.AdjustPointsRequest;
import com.group1.engagement_service.entity.LoyaltyBalance;
import com.group1.engagement_service.entity.LoyaltyRedemption;
import com.group1.engagement_service.entity.PointHistory;
import com.group1.engagement_service.entity.RedemptionStatus;
import com.group1.engagement_service.entity.Reward;
import com.group1.engagement_service.entity.Tier;
import com.group1.engagement_service.entity.TierBenefit;
import com.group1.engagement_service.repository.LoyaltyBalanceRepository;
import com.group1.engagement_service.repository.LoyaltyRedemptionRepository;
import com.group1.engagement_service.repository.PointHistoryRepository;
import com.group1.engagement_service.repository.RewardRepository;
import com.group1.engagement_service.repository.TierHistoryRepository;
import com.group1.engagement_service.repository.TierRepository;
import com.group1.engagement_service.request.RedeemRequest;
import com.group1.engagement_service.response.RedemptionResponse;
import com.group1.engagement_service.response.TierResponse;
import com.group1.engagement_service.security.AuthenticatedUser;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoyaltyService {

    private final LoyaltyBalanceRepository loyaltyBalanceRepository;
    private final RewardRepository rewardRepository;
    private final LoyaltyRedemptionRepository loyaltyRedemptionRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final TierHistoryRepository tierHistoryRepository;
    private final TierRepository tierRepository;
    private final CustomerSegmentFeignClient customerClient;


    public LoyaltyService(LoyaltyBalanceRepository loyaltyBalanceRepository, RewardRepository rewardRepository, LoyaltyRedemptionRepository loyaltyRedemptionRepository, PointHistoryRepository pointHistoryRepository, TierHistoryRepository tierHistoryRepository, TierRepository tierRepository, CustomerSegmentFeignClient customerClient) {
        this.loyaltyBalanceRepository = loyaltyBalanceRepository;
        this.rewardRepository = rewardRepository;
        this.loyaltyRedemptionRepository = loyaltyRedemptionRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.tierHistoryRepository = tierHistoryRepository;
        this.tierRepository = tierRepository;
        this.customerClient = customerClient;
    }

    public String redeem(Long customerId, RedeemRequest request, String token) {

        if ("CHECKOUT_DISCOUNT".equals(request.getType())) {
            return redeemDiscount(customerId, request, token);
        }

        return redeemReward(customerId, request);
    }

    @Transactional
    public String redeemReward(Long customerId, RedeemRequest request) {

        LoyaltyBalance balance = loyaltyBalanceRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Balance not found"));

        Reward reward = rewardRepository.findById(request.getRewardId())
                .orElseThrow(() -> new RuntimeException("Reward not found"));

        //Reward không available
        if (!reward.isAvailability()) {
            throw new RuntimeException("Reward not available");
        }

        //Không đủ điểm
        if (balance.getCurrentPoints() < reward.getRequiredPoints()) {
            throw new RuntimeException("Insufficient points");
        }

        //Trừ điểm
        balance.setCurrentPoints(balance.getCurrentPoints() - reward.getRequiredPoints());
        loyaltyBalanceRepository.save(balance);

        //Lưu history
        LoyaltyRedemption redemption = LoyaltyRedemption.builder()
                .customerId(customerId)
                .reward(reward)
                .pointsUsed(reward.getRequiredPoints())
                .status(RedemptionStatus.SUCCESS)
                .type(reward.getType().name())
                .createdAt(LocalDateTime.now())
                .build();

        loyaltyRedemptionRepository.save(redemption);

        PointHistory history = PointHistory.builder()
                .customerId(customerId)
                .type("REDEEM")
                .amount(-reward.getRequiredPoints()) // trừ điểm nên âm
                .reason("Redeem reward")
                .adjustedBy("SYSTEM")
                .build();

        pointHistoryRepository.save(history);

        return "Redeem success";
    }

    //AC 34.3 - Redeem Discount
    @Transactional
    public String redeemDiscount(Long customerId, RedeemRequest request, String token) {

        LoyaltyBalance balance = loyaltyBalanceRepository
                .findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Balance not found"));

        if (balance.getCurrentPoints() < request.getPoints()) {
            throw new RuntimeException("Insufficient points");
        }

        Reward reward = rewardRepository.findRewardByType(request.getType())
                .orElseThrow(() -> new RuntimeException("Reward not found"));

        //Reward không available
        if (!reward.isAvailability()) {
            throw new RuntimeException("Reward not available");
        }

        //Points < required
        if (request.getPoints() < reward.getRequiredPoints()) {
            throw new RuntimeException("Not enough points for this reward");
        }

        // Logic convert point → discount
        BigDecimal discountAmount = convertPointsToMoney(request.getPoints());

        // CALL ORDER SERVICE (quan trọng)
        applyDiscount(request.getOrderId(), discountAmount, token);

        // Trừ điểm
        balance.setCurrentPoints(balance.getCurrentPoints() - request.getPoints());
        loyaltyBalanceRepository.save(balance);

        // Log
        LoyaltyRedemption redemption = LoyaltyRedemption.builder()
                .customerId(customerId)
                .reward(reward)
                .pointsUsed(request.getPoints())
                .status(RedemptionStatus.SUCCESS)
                .type(reward.getType().name())
                .orderId(request.getOrderId())
                .description("Applied discount: " + discountAmount)
                .createdAt(LocalDateTime.now())
                .build();

        loyaltyRedemptionRepository.save(redemption);

        PointHistory history = PointHistory.builder()
                .customerId(customerId)
                .type("REDEEM")
                .amount(-request.getPoints()) // trừ điểm nên âm
                .reason("Redeem for discount")
                .orderId(request.getOrderId()) // nếu có
                .adjustedBy("SYSTEM")
                .build();

        pointHistoryRepository.save(history);
        System.out.println("DONE REDEEM");
        return "Discount applied";
    }

    private BigDecimal convertPointsToMoney(int points) {
        return BigDecimal.valueOf(points).multiply(BigDecimal.valueOf(100));
    }

    public void applyDiscount(Long orderId, BigDecimal amount, String token) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8082/customer/orders/"
                + orderId + "/apply-discount?amount=" + amount;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    public List<RedemptionResponse> getRedemptionHistory(Long customerId) {

        List<LoyaltyRedemption> list
                = loyaltyRedemptionRepository.findByCustomerId(customerId);

        return list.stream()
                .map(this::map)
                .toList();
    }

    public RedemptionResponse map(LoyaltyRedemption lr) {
        RedemptionResponse res = new RedemptionResponse();

        res.setId(lr.getId());
        res.setPointsUsed(lr.getPointsUsed());
        res.setStatus(lr.getStatus().name());
        res.setType(lr.getType());
        res.setCreatedAt(lr.getCreatedAt());

        if (lr.getReward() != null) {
            res.setRewardDescription(lr.getReward().getDescription());
        }

        return res;
    }

    public LoyaltyResponseDTO getLoyalty(Long customerId) {

        LoyaltyBalance loyalty = loyaltyBalanceRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Loyalty not found"));

        return buildResponse(loyalty);
    }

    // =============================
    // BUILD RESPONSE
    // =============================
    private LoyaltyResponseDTO buildResponse(LoyaltyBalance loyalty) {

        int currentPoints = loyalty.getCurrentPoints();
        int totaPoints = loyalty.getTotalPointsEarned();
        // FIX tier
        String currentTier = (loyalty.getCurrentTier() == null)
                ? "BRONZE"
                : loyalty.getCurrentTier().getName().toUpperCase();

        String nextTier = getNextTier(currentTier);

        int currentThreshold = getThreshold(currentTier);
        int nextThreshold = (nextTier == null) ? currentThreshold : getThreshold(nextTier);

        int pointsToNext = (nextTier == null)
                ? 0
                : Math.max(0, nextThreshold - totaPoints);

        int progress;
        if (nextTier == null) {
            progress = 100;
        } else {
            int range = nextThreshold - currentThreshold;
            int earned = totaPoints - currentThreshold;
            progress = (range > 0)
                    ? (int) (((double) Math.max(0, earned) / range) * 100)
                    : 0;
        }

        return LoyaltyResponseDTO.builder()
                .enrolled(true)
                .currentTier(buildTier(loyalty.getCurrentTier()))
                .currentPoints(currentPoints)
                .lifetimePoints(loyalty.getTotalPointsEarned()) // 🔥 FIX
                .tierProgress(
                        TierProgressDTO.builder()
                                .currentTier(currentTier)
                                .nextTier(nextTier != null ? nextTier : "MAX")
                                .currentPoints(currentPoints)
                                .pointsToNextTier(pointsToNext)
                                .currentThreshold(currentThreshold)
                                .nextThreshold(nextThreshold)
                                .progressPercentage(Math.min(100, progress))
                                .build()
                )
                .pointsExpiringSoon(null)
                .build();
    }

    // =============================
    // HELPER
    // =============================
    private String getNextTier(String currentTier) {
        return switch (currentTier) {
            case "BRONZE" ->
                "SILVER";
            case "SILVER" ->
                "GOLD";
            case "GOLD" ->
                "PLATINUM";
            case "PLATINUM" ->
                "DIAMOND";
            default ->
                null;
        };
    }

    private int getThreshold(String tier) {
        return switch (tier) {
            case "BRONZE" ->
                0;
            case "SILVER" ->
                1001;
            case "GOLD" ->
                5001;
            case "PLATINUM" ->
                15001;
            case "DIAMOND" ->
                50001;
            default ->
                0;
        };
    }

    private TierDTO buildTier(Tier tier) {

        if (tier == null) {
            return TierDTO.builder()
                    .name("BRONZE")
                    .minPoints(0)
                    .maxPoints(1000)
                    .benefits(List.of())
                    .build();
        }

        return TierDTO.builder()
                .name(tier.getName())
                .minPoints(tier.getMinPoints())
                .maxPoints(tier.getMaxPoints())
                .benefits(
                        tier.getBenefits().stream()
                                .map(this::mapBenefit)
                                .toList()
                )
                .build();
    }

    private TierBenefitDTO mapBenefit(TierBenefit b) {
        return TierBenefitDTO.builder()
                .type(b.getType())
                .value(b.getValue())
                .description(b.getDescription())
                .build();
    }

    public List<PointsHistoryDTO> getPointsHistory(Long id, String type,
            LocalDateTime from, LocalDateTime to,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        List<PointHistory> rawList = pointHistoryRepository
                .findByCustomerId(id, pageable)
                .getContent();

        return rawList.stream()
                // 🔥 filter type
                .filter(p -> type == null || p.getType().equalsIgnoreCase(type))
                // 🔥 filter time
                .filter(p -> from == null || !p.getCreatedAt().isBefore(from))
                .filter(p -> to == null || !p.getCreatedAt().isAfter(to))
                // 🔥 map DTO
                .map(p -> PointsHistoryDTO.builder()
                .type(p.getType()) // FIX
                .amount(p.getAmount())
                .description(p.getReason()) // FIX
                .orderId(p.getOrderId())
                .createdAt(p.getCreatedAt())
                .build())
                .toList();
    }

    public List<TierHistoryDTO> getTierHistory(Long id) {

        return tierHistoryRepository
                .findByCustomerIdOrderByChangedAtDesc(id)
                .stream()
                .map(t -> TierHistoryDTO.builder()
                .fromTier(t.getOldTier()) //
                .toTier(t.getNewTier()) //

                .changedAt(t.getChangedAt())
                .build())
                .toList();
    }

    public List<TierResponse> findAllTiers() {
        // 1. Lấy tất cả cấu hình hạng từ DB, sắp xếp theo số điểm tối thiểu
        List<Tier> tierConfigs = tierRepository.findAll(Sort.by(Sort.Direction.ASC, "minPoints"));

        // 2. Chuyển đổi từ Entity sang DTO (AC 35.1) 🍓
        return tierConfigs.stream().map(config -> {

            // Giả sử mỗi hạng có danh sách quyền lợi riêng
            List<TierBenefitDTO> benefitDTOs = config.getBenefits().stream()
                    .map(b -> TierBenefitDTO.builder()
                    .description(b.getDescription())
                    .type(b.getType())
                    .value(b.getValue())
                    .build())
                    .toList();

            return TierResponse.builder()
                    .name(config.getName())
                    .minPoints(config.getMinPoints())
                    .maxPoints(config.getMaxPoints())
                    .benefits(benefitDTOs)
                    .build();
        }).toList();
    }

    @org.springframework.transaction.annotation.Transactional
    public void adjustPoints(Long customerId, AdjustPointsRequest request, AuthenticatedUser actor, String authorizationHeader) {
        // Validate customer existence via customer-service (requires Authorization, can be skipped if not provided)
        // We try best-effort: if actor is present, we can only proceed; otherwise rely on local existence
        Optional<LoyaltyBalance> existing = loyaltyBalanceRepository.findByCustomerId(customerId);
        LoyaltyBalance balance = existing.orElseGet(() -> {
            // lazily create balance with default tier (lowest)
            Tier lowest = getLowestTier().orElseThrow(() -> new IllegalStateException("Tier configuration missing"));
            return loyaltyBalanceRepository.save(LoyaltyBalance.builder()
                    .customerId(customerId)
                    .currentTier(lowest)
                    .currentPoints(0)
                    .pendingPoints(0)
                    .totalPointsEarned(0)
                    .build());
        });

        int amount = request.getAmount();
        if (request.getType() == AdjustPointsRequest.AdjustType.ADD) {
            balance.setCurrentPoints(balance.getCurrentPoints() + amount);
            balance.setTotalPointsEarned(
                    (balance.getTotalPointsEarned() == null ? 0 : balance.getTotalPointsEarned()) + amount
            );
            loyaltyBalanceRepository.save(balance);
            savePointHistory(customerId, "ADD", amount, request.getReason(), actor);
        } else {
            // DEDUCT
            if (amount > balance.getCurrentPoints()) {
                throw new IllegalArgumentException("insufficient points");
            }
            balance.setCurrentPoints(balance.getCurrentPoints() - amount);
            loyaltyBalanceRepository.save(balance);
            savePointHistory(customerId, "DEDUCT", amount, request.getReason(), actor);
        }

        // Re-evaluate tier after adjustment
        reEvaluateAndNotifyIfChanged(balance, actor, authorizationHeader);
    }

    private void savePointHistory(Long customerId, String type, int amount, String reason, AuthenticatedUser actor) {
        pointHistoryRepository.save(PointHistory.builder()
                .customerId(customerId)
                .type(type)
                .amount(amount)
                .reason(reason)
                .adjustedBy(actor != null ? String.valueOf(actor.getUserId()) : null)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private Optional<Tier> getLowestTier() {
        List<Tier> tiers = tierRepository.findAllByOrderByMinPointsAsc();
        return tiers.isEmpty() ? Optional.empty() : Optional.of(tiers.get(0));
    }

    private void reEvaluateAndNotifyIfChanged(LoyaltyBalance balance, AuthenticatedUser actor, String authorizationHeader) {
        Tier currentTier = balance.getCurrentTier();
        // Re-evaluate based on CURRENT points to align with UI and summary
        int currentPts = balance.getCurrentPoints() == null ? 0 : balance.getCurrentPoints();
        Tier newTier = determineTierForPoints(currentPts, currentTier);
        if (!Objects.equals(currentTier, newTier)) {
            balance.setCurrentTier(newTier);
            loyaltyBalanceRepository.save(balance);
            // Best-effort: write tier history to customer-service
            try {
                if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                    java.util.Map<String, String> body = new java.util.HashMap<>();
                    body.put("fromTier", currentTier != null ? currentTier.getName() : "BRONZE");
                    body.put("toTier", newTier.getName());
                    // Use existing enum in customer-service: MANUAL_ADJUSTMENT
                    body.put("reason", "MANUAL_ADJUSTMENT");
                    customerClient.createTierHistory(balance.getCustomerId(), authorizationHeader, body);
                    // Always sync tier to ensure denormalized columns update even if history failed
                    java.util.Map<String, String> sync = new java.util.HashMap<>();
                    sync.put("tier", newTier.getName());
                    customerClient.syncTier(balance.getCustomerId(), authorizationHeader, sync);
                }
            } catch (Exception ignored) {
            }
        }
        // If no change, still sync current tier as safeguard
        else if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            try {
                java.util.Map<String, String> sync = new java.util.HashMap<>();
                sync.put("tier", currentTier != null ? currentTier.getName() : determineTierForPoints(currentPts, currentTier).getName());
                customerClient.syncTier(balance.getCustomerId(), authorizationHeader, sync);
            } catch (Exception ignored) {}
        }
    }

    private Tier determineTierForPoints(Integer totalPointsEarned, Tier currentIfUnknown) {
        List<Tier> tiers = tierRepository.findAllByOrderByMinPointsAsc();
        if (tiers.isEmpty()) {
            if (currentIfUnknown == null) throw new IllegalStateException("Tier configuration missing");
            return currentIfUnknown;
        }
        int points = totalPointsEarned == null ? 0 : totalPointsEarned;
        return tiers.stream()
                .filter(t -> points >= t.getMinPoints())
                .filter(t -> t.getMaxPoints() == null || points <= t.getMaxPoints())
                .max(Comparator.comparingInt(Tier::getMinPoints))
                .orElse(tiers.get(0));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.Map<String, Object> getLoyaltySummary(Long customerId) {
        LoyaltyBalance balance = loyaltyBalanceRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("customer not found"));
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        int currentPts = balance.getCurrentPoints() == null ? 0 : balance.getCurrentPoints();
        resp.put("current_points", currentPts);
        resp.put("total_points_earned", balance.getTotalPointsEarned());
        // Tier derived from CURRENT points to match UI expectation
        Tier tierByCurrent = determineTierForPoints(currentPts, balance.getCurrentTier());
        resp.put("tier", tierByCurrent != null ? tierByCurrent.getName() : null);
        return resp;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.Map<String, Object> getCurrentPointsSecure(
            Long customerId,
            AuthenticatedUser actor,
            boolean isAdmin,
            String authorizationHeader
    ) {
        if (actor == null) {
            throw new RuntimeException("Unauthorized");
        }
        if (!isAdmin) {
            Long ownCustomerId = resolveCustomerIdByUser(actor.getUserId(), authorizationHeader);
            if (ownCustomerId == null || !ownCustomerId.equals(customerId)) {
                throw new RuntimeException("Forbidden");
            }
        }

        LoyaltyBalance balance = loyaltyBalanceRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("customer not found"));
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("customer_id", customerId);
        Integer currentPts = balance.getCurrentPoints() == null ? 0 : balance.getCurrentPoints();
        resp.put("current_points", currentPts);
        Tier tierByCurrent = determineTierForPoints(currentPts, balance.getCurrentTier());
        resp.put("tier", tierByCurrent.getName());
        return resp;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.Map<String, Object> getMyCurrentPoints(AuthenticatedUser actor, String authorizationHeader) {
        if (actor == null) {
            throw new RuntimeException("Unauthorized");
        }
        Long ownCustomerId = resolveCustomerIdByUser(actor.getUserId(), authorizationHeader);
        if (ownCustomerId == null) {
            throw new RuntimeException("Customer not found for current user");
        }
        LoyaltyBalance balance = loyaltyBalanceRepository.findByCustomerId(ownCustomerId)
                .orElseThrow(() -> new EntityNotFoundException("customer not found"));
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("customer_id", ownCustomerId);
        Integer currentPts = balance.getCurrentPoints() == null ? 0 : balance.getCurrentPoints();
        resp.put("current_points", currentPts);
        Tier tierByCurrent = determineTierForPoints(currentPts, balance.getCurrentTier());
        resp.put("tier", tierByCurrent.getName());
        return resp;
    }

    private Long resolveCustomerIdByUser(Long userId, String authorizationHeader) {
        // 1) Fast-path: in some data setups, userId == customerId and balance already exists
        if (userId != null && loyaltyBalanceRepository.findByCustomerId(userId).isPresent()) {
            return userId;
        }
        if (authorizationHeader == null || authorizationHeader.isBlank()) return null;

        // 2) Preferred endpoint: /by-user/{userId}
        try {
            var body = customerClient.getCustomerByUserId(userId, authorizationHeader).getBody();
            Long id = extractCustomerId(body);
            if (id != null) return id;
        } catch (FeignException.NotFound ignored) {
            // fallback below
        } catch (Exception ignored) {}

        // 3) Fallback endpoint: /find-id/{userId}
        try {
            var body = customerClient.getCustomerIdByUserId(userId, authorizationHeader).getBody();
            if (body instanceof java.util.Map<?, ?> root) {
                Object idObj = root.get("id");
                if (idObj instanceof Number n) return n.longValue();
                if (idObj instanceof String s) {
                    try { return Long.valueOf(s); } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    private Long extractCustomerId(Object body) {
        if (!(body instanceof java.util.Map<?, ?> root)) return null;
        Object dataObj = root.get("data");
        if (!(dataObj instanceof java.util.Map<?, ?> data)) return null;
        Object customerIdObj = data.get("customerId");
        if (customerIdObj instanceof Number n) return n.longValue();
        if (customerIdObj instanceof String s) {
            try { return Long.valueOf(s); } catch (Exception ignored) {}
        }
        return null;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getLocalPointsHistory(Long customerId) {
        return pointHistoryRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(ph -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("type", ph.getType());
                    m.put("amount", ph.getAmount());
                    m.put("reason", ph.getReason());
                    m.put("adjustedBy", ph.getAdjustedBy());
                    m.put("createdAt", ph.getCreatedAt());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.Map<String, Object> getBalance(Long customerId, Integer recentLimit, Integer pointValue, String currency) {
        LoyaltyBalance balance = loyaltyBalanceRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("customer not found"));
        int currentPts = balance.getCurrentPoints() == null ? 0 : balance.getCurrentPoints();
        int pendingPts = balance.getPendingPoints() == null ? 0 : balance.getPendingPoints();

        // --- Expiring soon logic ---
        int expiryDays = 365; // điểm hết hạn sau 365 ngày
        int soonWindowDays = 30; // "sắp hết hạn" trong 30 ngày tới
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate soonLimit = today.plusDays(soonWindowDays);

        List<PointHistory> addHistories = pointHistoryRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .filter(ph -> "ADD".equalsIgnoreCase(ph.getType()))
                .toList();

        int expiringPoints = 0;
        java.time.LocalDate firstExpireDate = null;
        for (PointHistory ph : addHistories) {
            if (ph.getCreatedAt() == null) continue;
            java.time.LocalDate expiredAt = ph.getCreatedAt().toLocalDate().plusDays(expiryDays);
            if (!expiredAt.isBefore(today) && !expiredAt.isAfter(soonLimit)) {
                expiringPoints += ph.getAmount() == null ? 0 : ph.getAmount();
                if (firstExpireDate == null || expiredAt.isBefore(firstExpireDate)) {
                    firstExpireDate = expiredAt;
                }
            }
        }

        java.util.Map<String, Object> expiringSoon = new java.util.HashMap<>();
        expiringSoon.put("points", expiringPoints);
        expiringSoon.put("date", firstExpireDate);

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("current_points", currentPts);
        resp.put("pending_points", pendingPts);
        // available = current - pending
        resp.put("available_points", Math.max(0, currentPts - pendingPts));
        resp.put("expiring_soon", expiringSoon);

        // Recent activity
        List<java.util.Map<String, Object>> recent = pointHistoryRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .limit(recentLimit == null ? 5 : Math.max(1, recentLimit))
                .map(ph -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("type", ph.getType());
                    m.put("amount", ph.getAmount());
                    m.put("reason", ph.getReason());
                    m.put("createdAt", ph.getCreatedAt());
                    return m;
                }).toList();
        resp.put("recent_activity", recent);

        // Points value
        int valuePerPoint = pointValue == null ? 100 : pointValue;
        java.util.Map<String, Object> pointsValue = new java.util.HashMap<>();
        pointsValue.put("points_value_currency", currency == null ? "VND" : currency);
        pointsValue.put("current_points_value", currentPts * (long) valuePerPoint);
        resp.put("points_value", pointsValue);

        return resp;
    }
}
