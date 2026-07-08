package com.group1.customer_service.service;

import com.group1.customer_service.dto.request.CreateSegmentRequest;
import com.group1.customer_service.dto.request.UpdateSegmentRequest;
import com.group1.customer_service.dto.response.SegmentCustomerDTO;
import com.group1.customer_service.dto.response.SegmentListItemResponse;
import com.group1.customer_service.dto.response.SegmentResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SegmentService {
    SegmentResponse createSegment(CreateSegmentRequest request);
    Page<SegmentListItemResponse> getSegments(int page, int limit);
    SegmentResponse getSegmentById(Long id);
    SegmentResponse updateSegment(Long id, UpdateSegmentRequest request);
    void deleteSegment(Long id);
    List<SegmentCustomerDTO> getCustomersInSegment(Long id, int page, int limit, String authHeader);
}

