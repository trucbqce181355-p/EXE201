package com.group1.customer_service.service;

import com.group1.customer_service.dto.*;
import com.group1.customer_service.entity.*;
import com.group1.customer_service.repository.*;
import com.group1.customer_service.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyRepository loyaltyRepository;
    private final PointsHistoryRepository pointsHistoryRepository;
    private final TierHistoryRepository tierHistoryRepository;
    private final CustomerRepository customerRepository;


    // GET LOYALTY
 
    public LoyaltyResponseDTO getLoyalty(Long customerId) {
    return loyaltyRepository.findByCustomer_CustomerId(customerId)
            .map(this::buildResponse)
            .orElseThrow(() -> new RuntimeException("Loyalty not found for customerId: " + customerId));
}

    
    // GET POINT HISTORY

    public List<PointsHistoryDTO> getPointsHistory(Long id, String type,
            LocalDateTime from, LocalDateTime to,
            int page, int size) {
        //validateUserAccess(id);
        // Customer customer = resolveCustomer(id);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        List<PointsHistory> rawList = pointsHistoryRepository
                .findByCustomer_CustomerIdOrderByCreatedAtDesc(id, pageable)
                .getContent();

        return rawList.stream()
                .filter(p -> type == null || p.getType().name().equalsIgnoreCase(type))
                .filter(p -> from == null || !p.getCreatedAt().isBefore(from))
                .filter(p -> to == null || !p.getCreatedAt().isAfter(to))
                .map(p -> PointsHistoryDTO.builder()
                        .type(p.getType().name())
                        .amount(p.getAmount())
                        .description(p.getDescription())
                        .orderId(p.getOrderId())
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

 
    // GET TIER HISTORY`
 
    public List<TierHistoryDTO> getTierHistory(Long id) {
        //validateUserAccess(id);
        // Customer customer = resolveCustomer(id);

        return tierHistoryRepository
                .findByCustomer_CustomerIdOrderByChangedAtDesc(id)
                .stream()
                .map(t -> TierHistoryDTO.builder()
                        .fromTier(t.getFromTier().name())
                        .toTier(t.getToTier().name())
                        .reason(t.getReason().name())
                        .changedAt(t.getChangedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void createTierHistory(Long customerId, Tier from, Tier to, TierChangeReason reason) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        tierHistoryRepository.save(TierHistory.builder()
                .customer(customer)
                .fromTier(from)
                .toTier(to)
                .reason(reason)
                .changedAt(LocalDateTime.now())
                .build());

        // Sync loyalty.currentTier and customers.loyalty_tier
        loyaltyRepository.findByCustomer_CustomerId(customerId).ifPresent(l -> {
            l.setCurrentTier(to.name());
            loyaltyRepository.save(l);
        });
        customer.setLoyaltyTier(to.name());
        customerRepository.save(customer);
    }

    @Transactional
    public void syncTier(Long customerId, Tier tier) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        loyaltyRepository.findByCustomer_CustomerId(customerId).ifPresent(l -> {
            l.setCurrentTier(tier.name());
            loyaltyRepository.save(l);
        });
        customer.setLoyaltyTier(tier.name());
        customerRepository.save(customer);
    }

    // EARN POINTS
    
    @Transactional
    public void earnPoints(Long id, int points) {
        Loyalty loyalty = getOrCreateById(id);
System.out.println("loyalty: " + loyalty);
        loyalty.setCurrentPoints(loyalty.getCurrentPoints() + points);
        loyalty.setLifetimePoints(loyalty.getLifetimePoints() + points);
        loyalty.setLastEarnedAt(LocalDateTime.now());

        loyaltyRepository.save(loyalty);

        pointsHistoryRepository.save(PointsHistory.builder()
                .customer(loyalty.getCustomer())
                .type(PointType.EARNED)
                .amount(points)
                .description("Earn from order")
                .createdAt(LocalDateTime.now())
                .build());

        checkUpgrade(loyalty);
    }

    @Transactional
    public void redeemPoints(Long id, int points) {
        Loyalty loyalty = getOrCreateById(id);

        if (loyalty.getCurrentPoints() < points) {
            throw new IllegalArgumentException("Not enough points");
        }

        loyalty.setCurrentPoints(loyalty.getCurrentPoints() - points);
        loyaltyRepository.save(loyalty);

        pointsHistoryRepository.save(PointsHistory.builder()
                .customer(loyalty.getCustomer())
                .type(PointType.REDEEMED)
                .amount(points)
                .description("Redeem points")
                .createdAt(LocalDateTime.now())
                .build());
    }

    private Customer resolveCustomer(Long id) {
        var byUser = customerRepository.findByUserId(id);
        System.out.println(byUser.get());
        if (byUser.isPresent()) {
            return byUser.get();
        }
        

        var byCustomer = customerRepository.findById(id);
        if (byCustomer.isPresent()) {
            return byCustomer.get();
        }
        System.out.println(byCustomer.get());

        Customer c = new Customer();
        c.setUserId(id);
        c.setCustomerCode("CUST_" + id);
        return customerRepository.save(c);
    }

    private Loyalty getOrCreateById(Long id) {
        Customer customer = customerRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
                
        System.out.println("customer: " + customer);
        return loyaltyRepository
                .findByCustomer_CustomerId(id)
                .orElseGet(() -> {
                    Loyalty l = Loyalty.builder()
                            .customer(customer)
                            .currentPoints(0)
                            .lifetimePoints(0)
                            .currentTier("BRONZE")
                            .enrolledAt(LocalDateTime.now())
                            .build();

                    // Ensure customer's loyalty_tier defaults to BRONZE on creation
                    customer.setLoyaltyTier("BRONZE");
                    customerRepository.save(customer);
                    return loyaltyRepository.save(l);
                });
    }

    private void validateUserAccess(Long requestUserId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new RuntimeException("Unauthorized");
        }

        Long tokenUserId = user.getUserId();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (!tokenUserId.equals(requestUserId) && !isAdmin) {
            throw new RuntimeException("Forbidden");
        }
    }

    private void checkUpgrade(Loyalty loyalty) {
        String current = loyalty.getCurrentTier() == null ? "BRONZE" : loyalty.getCurrentTier();
        Tier oldTier = Tier.valueOf(current);
        Tier newTier = calculateTier(loyalty.getLifetimePoints());

        if (!oldTier.equals(newTier)) {
            tierHistoryRepository.save(TierHistory.builder()
                    .customer(loyalty.getCustomer())
                    .fromTier(oldTier)
                    .toTier(newTier)
                    .reason(TierChangeReason.POINTS_EARNED)
                    .changedAt(LocalDateTime.now())
                    .build());

            loyalty.setCurrentTier(newTier.name());
            loyaltyRepository.save(loyalty);

            // Sync customer's loyalty_tier column
            Customer c = loyalty.getCustomer();
            if (c != null) {
                c.setLoyaltyTier(newTier.name());
                customerRepository.save(c);
            }
        }
    }

    private Tier calculateTier(int points) {
        if (points >= 10000)
            return Tier.PLATINUM;
        if (points >= 5000)
            return Tier.GOLD;
        if (points >= 1000)
            return Tier.SILVER;
        return Tier.BRONZE;
    }

    private LoyaltyResponseDTO buildResponse(Loyalty loyalty) {
        int currentPoints = loyalty.getCurrentPoints();

        String currentTier = (loyalty.getCurrentTier() == null)
                ? "BRONZE"
                : loyalty.getCurrentTier().toUpperCase();

        String nextTier = getNextTier(currentTier);

        int currentThreshold = getThreshold(currentTier);
        int nextThreshold = (nextTier == null) ? currentThreshold : getThreshold(nextTier);

        int pointsToNext = (nextTier == null)
                ? 0
                : Math.max(0, nextThreshold - currentPoints);

        int progress;
        if (nextTier == null) {
            progress = 100;
        } else {
            int range = nextThreshold - currentThreshold;
            if (range > 0) {
                int earned = currentPoints - currentThreshold;
                progress = (int) (((double) Math.max(0, earned) / range) * 100);
            } else {
                progress = 0;
            }
        }

        return LoyaltyResponseDTO.builder()
                .enrolled(true)
                .enrolledAt(loyalty.getEnrolledAt())
                .currentTier(buildTier(currentTier))
                .currentPoints(currentPoints)
                .lifetimePoints(loyalty.getLifetimePoints())
                .tierProgress(
                        TierProgressDTO.builder()
                                .currentTier(currentTier)
                                .nextTier(nextTier != null ? nextTier : "MAX")
                                .currentPoints(currentPoints)
                                .pointsToNextTier(pointsToNext)
                                .currentThreshold(currentThreshold)
                                .nextThreshold(nextThreshold)
                                .progressPercentage(Math.min(100, progress))
                                .build())
                .build();
    }

    private String getNextTier(String tier) {
        switch (tier.toUpperCase()) {
            case "BRONZE":
                return "SILVER";
            case "SILVER":
                return "GOLD";
            case "GOLD":
                return "PLATINUM";
            default:
                return null;
        }
    }

    private int getThreshold(String tier) {
        if (tier == null)
            return 0;
        switch (tier.toUpperCase()) {
            case "PLATINUM":
                return 10000;
            case "GOLD":
                return 5000;
            case "SILVER":
                return 1000;
            default:
                return 0;
        }
    }

    private TierDTO buildTier(String tier) {
        return TierDTO.builder()
                .code(tier)
                .name(getTierName(tier))
                .benefits(getBenefits(tier))
                .build();
    }

    private String getTierName(String tier) {
        switch (tier.toUpperCase()) {
            case "PLATINUM":
                return "Thành viên Bạch Kim";
            case "GOLD":
                return "Thành viên Vàng";
            case "SILVER":
                return "Thành viên Bạc";
            default:
                return "Thành viên Đồng";
        }
    }

    private TierBenefitsDTO getBenefits(String tier) {
        switch (tier.toUpperCase()) {
            case "PLATINUM":
                return TierBenefitsDTO.builder()
                        .discountPercentage(20)
                        .freeShippingThreshold(0)
                        .birthdayBonusPoints(500)
                        .exclusivePromotions(true)
                        .build();
            case "GOLD":
                return TierBenefitsDTO.builder()
                        .discountPercentage(15)
                        .freeShippingThreshold(200000)
                        .birthdayBonusPoints(200)
                        .exclusivePromotions(true)
                        .build();
            case "SILVER":
                return TierBenefitsDTO.builder()
                        .discountPercentage(10)
                        .freeShippingThreshold(300000)
                        .birthdayBonusPoints(100)
                        .exclusivePromotions(false)
                        .build();
            default:
                return TierBenefitsDTO.builder()
                        .discountPercentage(5)
                        .freeShippingThreshold(500000)
                        .birthdayBonusPoints(50)
                        .exclusivePromotions(false)
                        .build();
        }
    }

    
}