package com.group1.engagement_service.repository;

import com.group1.engagement_service.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 🔥 Lấy history theo customer (dùng cho AC 34.5 sau này)
    List<PointHistory> findByCustomerId(Long customerId);

    // 🔥 Lấy history REDEEM (lọc theo type)
    List<PointHistory> findByCustomerIdAndType(Long customerId, String type);

    // 🔥 lấy lịch sử redeem theo customer
    List<PointHistory> findByCustomerIdAndTypeOrderByCreatedAtDesc(Long customerId, String type);

    Page<PointHistory> findByCustomerId(Long customerId, Pageable pageable);

    List<PointHistory> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("""
                SELECT COALESCE(SUM(ph.amount),0)
                FROM PointHistory ph
                WHERE ph.type IN ('EARN','ADJUSTMENT') AND ph.amount > 0
            """)
    long totalPointsIssued();

    @Query("""
                SELECT COALESCE(SUM(ABS(ph.amount)),0)
                FROM PointHistory ph
                WHERE ph.type = 'REDEEM'
            """)
    long totalPointsRedeemed();

    List<PointHistory> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
