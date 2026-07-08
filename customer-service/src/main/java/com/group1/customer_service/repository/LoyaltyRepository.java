package com.group1.customer_service.repository;

import com.group1.customer_service.entity.Loyalty;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltyRepository extends JpaRepository<Loyalty, Long> {

    Optional<Loyalty> findByCustomer_CustomerId(Long customerId);

    

}