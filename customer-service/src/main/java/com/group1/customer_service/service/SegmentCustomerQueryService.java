package com.group1.customer_service.service;

import com.group1.customer_service.dto.request.SegmentCriteriaDTO;
import com.group1.customer_service.dto.response.SegmentCustomerDTO;

import java.util.List;

public interface SegmentCustomerQueryService {
    List<SegmentCustomerDTO> findCustomers(SegmentCriteriaDTO criteria, int page, int limit, String authHeader);
    long countCustomers(SegmentCriteriaDTO criteria);

    /** Cùng pipeline SQL với {@link #countCustomers}, chỉ kiểm tra một {@code customerId}. */
    boolean customerMatchesSegmentCriteria(SegmentCriteriaDTO criteria, Long customerId);

    boolean isInSegment(List<Long> segmentIds, Long customerId);

    long countCustomersBySegments(List<Long> ids, String mode);
}

