package com.group1.customer_service.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.group1.customer_service.service.SegmentCustomerQueryService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.group1.customer_service.dto.request.CreateSegmentRequest;
import com.group1.customer_service.dto.request.UpdateSegmentRequest;
import com.group1.customer_service.dto.response.SegmentCustomerDTO;
import com.group1.customer_service.dto.response.SegmentListItemResponse;
import com.group1.customer_service.dto.response.SegmentResponse;
import com.group1.customer_service.security.AuthenticatedUser;
import com.group1.customer_service.service.CustomerService;
import com.group1.customer_service.service.SegmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/customer-segments")
@RequiredArgsConstructor
public class SegmentController {

    private final SegmentService segmentService;
    private final SegmentCustomerQueryService segmentCustomerQueryService;
    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAuthority('SEGMENT:CREATE')")
    public ResponseEntity<SegmentResponse> createSegment(@Valid @RequestBody CreateSegmentRequest request) {
        SegmentResponse created = segmentService.createSegment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SEGMENT:READ')")
    public ResponseEntity<Map<String, Object>> getSegments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Page<SegmentListItemResponse> result = segmentService.getSegments(page, limit);
        Map<String, Object> response = new HashMap<>();
        response.put("data", result.getContent());
        response.put("total", result.getTotalElements());
        response.put("page", page);
        response.put("limit", limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SEGMENT:READ')")
    public ResponseEntity<SegmentResponse> getSegmentDetail(@PathVariable Long id) {
        SegmentResponse result = segmentService.getSegmentById(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SEGMENT:UPDATE')")
    public ResponseEntity<SegmentResponse> updateSegment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSegmentRequest request) {
        SegmentResponse updated = segmentService.updateSegment(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SEGMENT:DELETE')")
    public ResponseEntity<Map<String, Object>> deleteSegment(@PathVariable Long id) {
        segmentService.deleteSegment(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Segment deleted");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}/customers")
    @PreAuthorize("hasAuthority('SEGMENT:READ')")
    public ResponseEntity<Map<String, Object>> getCustomersInSegment(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("Authorization") String authHeader) {
        List<SegmentCustomerDTO> customers = segmentService.getCustomersInSegment(id, page, limit, authHeader);
        Map<String, Object> response = new HashMap<>();
        response.put("data", customers);
        response.put("page", page);
        response.put("limit", limit);
        response.put("total", customers.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Khách đã đăng nhập (JWT) kiểm tra chính mình có thuộc segment (theo tiêu chí động) hay không.
     * Không cần {@code SEGMENT:READ} — engagement-service dùng cho promotion targeting.
     */
    @GetMapping("/membership/{customerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getSegmentMembershipForCustomer(
            @RequestParam List<Long> ids,
            @PathVariable Long customerId,
            Authentication authentication) {

        if (!(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var customer = customerService.getByUserId(user.getUserId());
        if (customer == null || !customer.getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean member = segmentCustomerQueryService.isInSegment(ids, customerId);

        Map<String, Object> body = new HashMap<>();
        body.put("member", member);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/customers/count-by-segments")
    public ResponseEntity<Map<String, Long>> countCustomersBySegments(
            @RequestParam List<Long> ids,
            @RequestParam String mode) {

        long count = segmentCustomerQueryService.countCustomersBySegments(ids, mode);

        return ResponseEntity.ok(Map.of("count", count));
    }
    @GetMapping("/api/customers/find-id/{userId}")
    public ResponseEntity<?> getCustomerId(@PathVariable Long userId) {
        try {
            Long customerId = customerService.getCustomerIdByUserId(userId);

            return ResponseEntity.ok(Map.of("id", customerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
