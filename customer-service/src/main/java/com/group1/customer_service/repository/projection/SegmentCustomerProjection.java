package com.group1.customer_service.repository.projection;

import java.time.LocalDate;

public interface SegmentCustomerProjection {
    Long getId();
    Long getUserId();
    Double getTotalSpent();
    Long getOrderCount();
    LocalDate getLastOrderDate();
    String getLoyaltyTier();
    String getLocation();
}

