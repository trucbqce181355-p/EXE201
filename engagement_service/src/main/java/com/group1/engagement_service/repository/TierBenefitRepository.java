package com.group1.engagement_service.repository;

import com.group1.engagement_service.entity.TierBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TierBenefitRepository extends JpaRepository<TierBenefit, Long> {
    List<TierBenefit> findByTierIdOrderByIdAsc(Long tierId);
    Optional<TierBenefit> findByIdAndTierId(Long id, Long tierId);
}
