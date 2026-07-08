package com.group1.customer_service.repository;

import com.group1.customer_service.entity.AddressOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AddressOrderRepository extends JpaRepository<AddressOrder, Long> {
    List<AddressOrder> findByCustomer_CustomerIdOrderByIsDefaultDescAddressIdAsc(Long customerId);

    long countByCustomer_CustomerId(Long customerId);

    @Modifying
    @Query("UPDATE AddressOrder a SET a.isDefault = false WHERE a.customer.customerId = :customerId")
    void resetDefaultAddressForCustomer(@Param("customerId") Long customerId);
}
