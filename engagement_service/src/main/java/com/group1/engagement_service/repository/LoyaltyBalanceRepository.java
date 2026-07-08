package com.group1.engagement_service.repository;

import com.group1.engagement_service.entity.LoyaltyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LoyaltyBalanceRepository extends JpaRepository<LoyaltyBalance, Long> {

    Optional<LoyaltyBalance> findByCustomerId(Long customerId);

    boolean existsByCurrentTier_Id(Long tierId);

    @Query(value = "SELECT COUNT(*) FROM loyalty_balances", nativeQuery = true)
    long countTotalMembers();

    @Query(value = "SELECT COUNT(*) FROM loyalty_balances WHERE current_points > 0", nativeQuery = true)
    long countActiveMembers();

    @Query(value = "SELECT t.name, COUNT(lb.id) " +
            "FROM loyalty_balances lb " +
            "JOIN tiers t ON lb.tier_id = t.id " +
            "GROUP BY t.name", nativeQuery = true)
    List<Object[]> countMembersByTier();
}