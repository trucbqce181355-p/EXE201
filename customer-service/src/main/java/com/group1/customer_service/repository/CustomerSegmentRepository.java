package com.group1.customer_service.repository;

import com.group1.customer_service.entity.CustomerSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerSegmentRepository extends JpaRepository<CustomerSegment, Long> {
    boolean existsByName(String name);
}

