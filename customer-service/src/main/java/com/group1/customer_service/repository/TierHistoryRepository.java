package com.group1.customer_service.repository;

import com.group1.customer_service.entity.TierHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TierHistoryRepository extends JpaRepository<TierHistory, Long> {

    // Lấy tất cả không phân trang
    List<TierHistory> findByCustomer_CustomerIdOrderByChangedAtDesc(Long customerId);

    // Lấy có phân trang
    List<TierHistory> findByCustomer_CustomerIdOrderByChangedAtDesc(Long customerId, Pageable pageable);
}