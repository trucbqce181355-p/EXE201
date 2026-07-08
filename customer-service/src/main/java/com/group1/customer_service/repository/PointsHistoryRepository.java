package com.group1.customer_service.repository;

import com.group1.customer_service.entity.PointsHistory;
import com.group1.customer_service.entity.PointType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface PointsHistoryRepository extends JpaRepository<PointsHistory, Long> {

    
    Page<PointsHistory> findByCustomer_CustomerIdOrderByCreatedAtDesc(
            Long customerId, Pageable pageable);

    
    List<PointsHistory> findByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);

    List<PointsHistory> findByCustomer_CustomerIdAndCreatedAtAfter(
            Long customerId, LocalDateTime date);

    
    List<PointsHistory> findByCustomer_CustomerIdAndType(
            Long customerId, PointType type);


    Page<PointsHistory> findByCustomer_CustomerIdAndTypeOrderByCreatedAtDesc(
            Long customerId, PointType type, Pageable pageable);

    
    Page<PointsHistory> findByCustomer_CustomerIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long customerId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    
    Page<PointsHistory> findByCustomer_CustomerIdAndTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long customerId, PointType type, LocalDateTime from, LocalDateTime to, Pageable pageable);
}