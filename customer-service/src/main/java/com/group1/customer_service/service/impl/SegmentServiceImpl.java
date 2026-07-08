package com.group1.customer_service.service.impl;

import com.group1.customer_service.dto.request.CreateSegmentRequest;
import com.group1.customer_service.dto.request.SegmentCriteriaDTO;
import com.group1.customer_service.dto.request.SegmentLogic;
import com.group1.customer_service.dto.request.UpdateSegmentRequest;
import com.group1.customer_service.dto.response.SegmentCustomerDTO;
import com.group1.customer_service.dto.response.SegmentListItemResponse;
import com.group1.customer_service.dto.response.SegmentResponse;
import com.group1.customer_service.entity.CustomerSegment;
import com.group1.customer_service.repository.CustomerSegmentRepository;
import com.group1.customer_service.service.SegmentCustomerQueryService;
import com.group1.customer_service.service.SegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SegmentServiceImpl implements SegmentService {

    private final CustomerSegmentRepository segmentRepository;
    private final SegmentCustomerQueryService customerQueryService;
    private static final Set<String> NUMBER_DATE_OPS = Set.of(">", "<", ">=", "<=", "=");
    private static final Set<String> TEXT_OPS = Set.of("IN", "NOT_IN");

    @Override
    public SegmentResponse createSegment(CreateSegmentRequest request) {
        if (segmentRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Segment name already exists");
        }
        SegmentCriteriaDTO criteria = normalizeAndValidateCriteria(request.getCriteria());
        CustomerSegment segment = CustomerSegment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logicOperator(criteria.getLogic().name())
                .totalSpent(criteria.getTotalSpent())
                .conditionSpent(criteria.getConditionSpent())
                .orderCount(criteria.getOrderCount())
                .conditionCount(criteria.getConditionCount())
                .lastOrderDate(criteria.getLastOrderDate())
                .conditionDate(criteria.getConditionDate())
                .loyaltyTier(criteria.getLoyaltyTier())
                .conditionTier(criteria.getConditionTier())
                .location(criteria.getLocation())
                .conditionLocation(criteria.getConditionLocation())
                .build();
        CustomerSegment saved = segmentRepository.save(segment);
        return toResponse(saved, criteria, customerQueryService.countCustomers(criteria));
    }

    @Override
    public Page<SegmentListItemResponse> getSegments(int page, int limit) {
        PageRequest pr = PageRequest.of(Math.max(page - 1, 0), limit);
        Page<CustomerSegment> segPage = segmentRepository.findAll(pr);
        List<SegmentListItemResponse> items = segPage.getContent().stream()
                .map(s -> {
                    SegmentCriteriaDTO criteria = mapEntityToCriteria(s);
                    return SegmentListItemResponse.builder()
                            .segmentId(s.getId())
                            .name(s.getName())
                            .customerCount(customerQueryService.countCustomers(criteria))
                            .createdAt(s.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
        return new PageImpl<>(items, pr, segPage.getTotalElements());
    }

    @Override
    public SegmentResponse getSegmentById(Long id) {
        CustomerSegment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Segment not found"));
        SegmentCriteriaDTO criteria = mapEntityToCriteria(segment);
        return toResponse(segment, criteria, customerQueryService.countCustomers(criteria));
    }

    @Override
    public SegmentResponse updateSegment(Long id, UpdateSegmentRequest request) {
        CustomerSegment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Segment not found"));

        if (!segment.getName().equals(request.getName()) && segmentRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Segment name already exists");
        }
        SegmentCriteriaDTO criteria = normalizeAndValidateCriteria(request.getCriteria());
        segment.setName(request.getName());
        segment.setDescription(request.getDescription());
        segment.setLogicOperator(criteria.getLogic().name());
        segment.setTotalSpent(criteria.getTotalSpent());
        segment.setConditionSpent(criteria.getConditionSpent());
        segment.setOrderCount(criteria.getOrderCount());
        segment.setConditionCount(criteria.getConditionCount());
        segment.setLastOrderDate(criteria.getLastOrderDate());
        segment.setConditionDate(criteria.getConditionDate());
        segment.setLoyaltyTier(criteria.getLoyaltyTier());
        segment.setConditionTier(criteria.getConditionTier());
        segment.setLocation(criteria.getLocation());
        segment.setConditionLocation(criteria.getConditionLocation());
        CustomerSegment saved = segmentRepository.save(segment);
        return toResponse(saved, criteria, customerQueryService.countCustomers(criteria));
    }

    @Override
    public void deleteSegment(Long id) {
        CustomerSegment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Segment not found"));
        segmentRepository.delete(segment);
    }

    @Override
    public List<SegmentCustomerDTO> getCustomersInSegment(Long id, int page, int limit, String authHeader) {
        CustomerSegment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Segment not found"));
        SegmentCriteriaDTO criteria = criteriaFromEntity(segment);
        return customerQueryService.findCustomers(criteria, page, limit, authHeader);
    }



    private SegmentCriteriaDTO criteriaFromEntity(CustomerSegment segment) {
        return SegmentCriteriaDTO.builder()
                .logic("OR".equalsIgnoreCase(segment.getLogicOperator()) ? SegmentLogic.OR : SegmentLogic.AND)
                .totalSpent(segment.getTotalSpent())
                .conditionSpent(segment.getConditionSpent())
                .orderCount(segment.getOrderCount())
                .conditionCount(segment.getConditionCount())
                .lastOrderDate(segment.getLastOrderDate())
                .conditionDate(segment.getConditionDate())
                .loyaltyTier(segment.getLoyaltyTier())
                .conditionTier(segment.getConditionTier())
                .location(segment.getLocation())
                .conditionLocation(segment.getConditionLocation())
                .build();
    }

    private SegmentCriteriaDTO normalizeAndValidateCriteria(SegmentCriteriaDTO criteria) {
        if (criteria == null || criteria.getLogic() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid criteria format");
        }
        if (criteria.getTotalSpent() == null
                && criteria.getOrderCount() == null
                && criteria.getLastOrderDate() == null
                && (criteria.getLoyaltyTier() == null || criteria.getLoyaltyTier().isBlank())
                && (criteria.getLocation() == null || criteria.getLocation().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid criteria format");
        }

        if (criteria.getTotalSpent() != null) {
            if (criteria.getConditionSpent() == null || criteria.getConditionSpent().isBlank()) {
                criteria.setConditionSpent("=");
            }
            criteria.setConditionSpent(criteria.getConditionSpent().trim());
            if (!NUMBER_DATE_OPS.contains(criteria.getConditionSpent())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid criteria format");
            }
        }
        if (criteria.getOrderCount() != null) {
            if (criteria.getConditionCount() == null || criteria.getConditionCount().isBlank()) {
                criteria.setConditionCount("=");
            }
            criteria.setConditionCount(criteria.getConditionCount().trim());
            if (!NUMBER_DATE_OPS.contains(criteria.getConditionCount())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid criteria format");
            }
        }
        if (criteria.getLastOrderDate() != null) {
            if (criteria.getConditionDate() == null || criteria.getConditionDate().isBlank()) {
                criteria.setConditionDate("=");
            }
            criteria.setConditionDate(criteria.getConditionDate().trim());
            if (!NUMBER_DATE_OPS.contains(criteria.getConditionDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid criteria format");
            }
        }
        if (criteria.getLoyaltyTier() != null && !criteria.getLoyaltyTier().isBlank()) {
            if (criteria.getConditionTier() == null || criteria.getConditionTier().isBlank()) {
                criteria.setConditionTier("IN");
            }
            criteria.setConditionTier(criteria.getConditionTier().trim().toUpperCase());
            if (!TEXT_OPS.contains(criteria.getConditionTier())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid criteria format");
            }
        }
        if (criteria.getLocation() != null && !criteria.getLocation().isBlank()) {
            if (criteria.getConditionLocation() == null || criteria.getConditionLocation().isBlank()) {
                criteria.setConditionLocation("IN");
            }
            criteria.setConditionLocation(criteria.getConditionLocation().trim().toUpperCase());
            if (!TEXT_OPS.contains(criteria.getConditionLocation())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid criteria format");
            }
        }
        return criteria;
    }

    private SegmentResponse toResponse(CustomerSegment s, SegmentCriteriaDTO criteria, long customerCount) {
        return SegmentResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .criteria(criteria)
                .customerCount(customerCount)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private SegmentCriteriaDTO mapEntityToCriteria(CustomerSegment segment) {
        return SegmentCriteriaDTO.builder()
                .logic("OR".equalsIgnoreCase(segment.getLogicOperator()) ? SegmentLogic.OR : SegmentLogic.AND)
                .totalSpent(segment.getTotalSpent())
                .conditionSpent(segment.getConditionSpent())
                .orderCount(segment.getOrderCount())
                .conditionCount(segment.getConditionCount())
                .lastOrderDate(segment.getLastOrderDate())
                .conditionDate(segment.getConditionDate())
                .loyaltyTier(segment.getLoyaltyTier())
                .conditionTier(segment.getConditionTier())
                .location(segment.getLocation())
                .conditionLocation(segment.getConditionLocation())
                .build();
    }
}

