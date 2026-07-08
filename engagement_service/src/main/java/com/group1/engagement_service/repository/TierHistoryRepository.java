package com.group1.engagement_service.repository;

import com.group1.engagement_service.entity.TierHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TierHistoryRepository extends JpaRepository<TierHistory, Long> {

    List<TierHistory> findByCustomerIdOrderByChangedAtDesc(Long customerId);
    
}