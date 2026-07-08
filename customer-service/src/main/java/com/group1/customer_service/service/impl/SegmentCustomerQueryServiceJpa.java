package com.group1.customer_service.service.impl;

import com.group1.customer_service.client.AuthClient;
import com.group1.customer_service.dto.request.SegmentCriteriaDTO;
import com.group1.customer_service.dto.request.SegmentLogic;
import com.group1.customer_service.dto.response.AuthUserBasicDTO;
import com.group1.customer_service.dto.response.SegmentCustomerDTO;
import com.group1.customer_service.entity.CustomerSegment;
import com.group1.customer_service.repository.CustomerRepository;
import com.group1.customer_service.repository.CustomerSegmentRepository;
import com.group1.customer_service.repository.OrderRepository;
import com.group1.customer_service.repository.projection.SegmentCustomerProjection;
import com.group1.customer_service.service.SegmentCustomerQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SegmentCustomerQueryServiceJpa implements SegmentCustomerQueryService {

    private final OrderRepository orderRepository;
    private final AuthClient authClient;
    private final CustomerRepository customerRepository;
    private final CustomerSegmentRepository segmentRepository;

    @Override
    public List<SegmentCustomerDTO> findCustomers(SegmentCriteriaDTO criteria, int page, int limit, String authHeader) {
        int safePage = Math.max(page, 1);
        int safeLimit = Math.max(limit, 1);
        List<SegmentCustomerProjection> rows = orderRepository.findSegmentCustomers(
                criteria.getLogic().name(),
                criteria.getTotalSpent(),
                defaultNumberDateOp(criteria.getConditionSpent()),
                criteria.getOrderCount() == null ? null : criteria.getOrderCount().longValue(),
                defaultNumberDateOp(criteria.getConditionCount()),
                criteria.getLastOrderDate(),
                defaultNumberDateOp(criteria.getConditionDate()),
                normalizeCsv(criteria.getLoyaltyTier()),
                defaultTextOp(criteria.getConditionTier()),
                normalizeCsv(criteria.getLocation()),
                defaultTextOp(criteria.getConditionLocation()),
                PageRequest.of(safePage - 1, safeLimit)
        );

        List<Long> userIds = rows.stream()
                .map(SegmentCustomerProjection::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, AuthUserBasicDTO> userInfoById = userIds.isEmpty()
                ? Map.of()
                : authClient.getUsersBasic(authHeader, userIds).stream()
                        .filter(u -> u.getId() != null)
                        .collect(Collectors.toMap(AuthUserBasicDTO::getId, Function.identity(), (a, b) -> a));

        return rows.stream().map(row -> toDto(row, userInfoById.get(row.getUserId()))).collect(Collectors.toList());
    }

    @Override
    public long countCustomers(SegmentCriteriaDTO criteria) {
        return orderRepository.countSegmentCustomers(
                criteria.getLogic().name(),
                criteria.getTotalSpent(),
                defaultNumberDateOp(criteria.getConditionSpent()),
                criteria.getOrderCount() == null ? null : criteria.getOrderCount().longValue(),
                defaultNumberDateOp(criteria.getConditionCount()),
                criteria.getLastOrderDate(),
                defaultNumberDateOp(criteria.getConditionDate()),
                normalizeCsv(criteria.getLoyaltyTier()),
                defaultTextOp(criteria.getConditionTier()),
                normalizeCsv(criteria.getLocation()),
                defaultTextOp(criteria.getConditionLocation())
        );
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
    @Override
    public boolean isInSegment(List<Long> segmentIds, Long customerId) {
        List<CustomerSegment> segments = segmentRepository.findAllById(segmentIds);

        for (CustomerSegment s : segments) {
            SegmentCriteriaDTO criteria = mapEntityToCriteria(s);

            Integer exists = orderRepository.existsCustomerInSegment(
                    customerId,
                    criteria.getLogic().name(),
                    criteria.getTotalSpent(),
                    defaultNumberDateOp(criteria.getConditionSpent()),
                    criteria.getOrderCount() == null ? null : criteria.getOrderCount().longValue(),
                    defaultNumberDateOp(criteria.getConditionCount()),
                    criteria.getLastOrderDate(),
                    defaultNumberDateOp(criteria.getConditionDate()),
                    normalizeCsv(criteria.getLoyaltyTier()),
                    defaultTextOp(criteria.getConditionTier()),
                    normalizeCsv(criteria.getLocation()),
                    defaultTextOp(criteria.getConditionLocation())
            );

            if (exists != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public long countCustomersBySegments(List<Long> segmentIds, String mode) {

        if (segmentIds == null || segmentIds.isEmpty()) {
            return customerRepository.count();
        }

        long segmentCount = countCustomersInSegments(segmentIds);

        if ("INCLUSIVE".equalsIgnoreCase(mode)) {
            return segmentCount;
        }

        // EXCLUSIVE
        long total = customerRepository.count();
        return total - segmentCount;
    }
    public long countCustomersInSegments(List<Long> segmentIds) {
        List<CustomerSegment> segments = segmentRepository.findAllById(segmentIds);

        Set<Long> uniqueCustomers = new HashSet<>();

        for (CustomerSegment s : segments) {
            SegmentCriteriaDTO c = mapEntityToCriteria(s);

            List<SegmentCustomerProjection> list =
                    orderRepository.findSegmentCustomers(
                            c.getLogic().name(),
                            c.getTotalSpent(),
                            defaultNumberDateOp(c.getConditionSpent()),
                            c.getOrderCount() == null ? null : c.getOrderCount().longValue(),
                            defaultNumberDateOp(c.getConditionCount()),
                            c.getLastOrderDate(),
                            defaultNumberDateOp(c.getConditionDate()),
                            normalizeCsv(c.getLoyaltyTier()),
                            defaultTextOp(c.getConditionTier()),
                            normalizeCsv(c.getLocation()),
                            defaultTextOp(c.getConditionLocation()),
                            Pageable.unpaged()
                    );

            for (SegmentCustomerProjection p : list) {
                uniqueCustomers.add(p.getId());
            }
        }

        return uniqueCustomers.size();
    }
    @Override
    public boolean customerMatchesSegmentCriteria(SegmentCriteriaDTO criteria, Long customerId) {
        if (customerId == null) {
            return false;
        }
        long n = orderRepository.countSegmentCustomersForCustomer(
                customerId,
                criteria.getLogic().name(),
                criteria.getTotalSpent(),
                defaultNumberDateOp(criteria.getConditionSpent()),
                criteria.getOrderCount() == null ? null : criteria.getOrderCount().longValue(),
                defaultNumberDateOp(criteria.getConditionCount()),
                criteria.getLastOrderDate(),
                defaultNumberDateOp(criteria.getConditionDate()),
                normalizeCsv(criteria.getLoyaltyTier()),
                defaultTextOp(criteria.getConditionTier()),
                normalizeCsv(criteria.getLocation()),
                defaultTextOp(criteria.getConditionLocation())
        );
        return n > 0;
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String defaultNumberDateOp(String op) {
        return op == null || op.isBlank() ? "=" : op;
    }

    private String defaultTextOp(String op) {
        return op == null || op.isBlank() ? "IN" : op;
    }

    private String normalizeCsv(String value) {
        String normalized = normalizeBlank(value);
        if (normalized == null) {
            return null;
        }
        return normalized.replace(", ", ",").replace(" ,", ",");
    }

    private SegmentCustomerDTO toDto(SegmentCustomerProjection p, AuthUserBasicDTO userInfo) {
        return SegmentCustomerDTO.builder()
                .id(p.getId())
                .name(userInfo != null ? userInfo.getFullName() : null)
                .email(userInfo != null ? userInfo.getEmail() : null)
                .totalSpent(p.getTotalSpent())
                .orderCount(p.getOrderCount())
                .lastOrderDate(p.getLastOrderDate())
                .loyaltyTier(p.getLoyaltyTier())
                .location(p.getLocation())
                .build();
    }
}

