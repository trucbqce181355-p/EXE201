package com.group1.engagement_service.repository;

import com.group1.engagement_service.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TierRepository extends JpaRepository<Tier, Long> {

    // Nếu cần chỉ lấy tất cả tiers
    List<Tier> findAll();
    Optional<Tier> findByNameIgnoreCase(String name);
    Optional<Tier> findByName(String name);
    List<Tier> findAllByOrderByMinPointsAsc();
    // Có thể thêm các method khác nếu muốn filter theo điểm hoặc name
    // List<Tier> findByMinPointsLessThanEqualAndMaxPointsGreaterThanEqual(int pointsMin, int pointsMax);
}