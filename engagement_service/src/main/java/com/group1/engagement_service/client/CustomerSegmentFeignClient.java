package com.group1.engagement_service.client;

import com.group1.engagement_service.dto.external.CustomerSegmentDetailDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "customer-service", url = "${services.customer-service.url}", path = "/customer-segments")
public interface CustomerSegmentFeignClient {

    @GetMapping("/{id}")
    CustomerSegmentDetailDto getSegment(
            @PathVariable("id") Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization);

    @GetMapping("/{id}/customers")
    Map<String, Object> getCustomersInSegment(
            @PathVariable("id") Long id,
            @RequestParam("page") int page,
            @RequestParam("limit") int limit,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization);

    /** Khách đã đăng nhập — không cần SEGMENT:READ (dùng cho promotion eligibility). */
    @GetMapping("/membership/{customerId}")
    Map<String, Object> getSegmentMembership(
            @RequestParam("ids") List<Long> ids,
            @PathVariable("customerId") Long customerId,
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/customers/count-by-segments")
    Map<String, Long> countCustomersBySegments(
            @RequestParam("ids") List<Long> ids,
            @RequestParam("mode") String mode,
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/api/customers/find-id/{userId}")
    Map<String, Object> findCustomerIdByUserId(
            @PathVariable("userId") Long userId,
            @RequestHeader("Authorization") String authorization);

    @GetMapping("/api/customers/{id}")
    ResponseEntity<?> getCustomerProfile(
            @PathVariable("id") Long customerId,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @GetMapping("/api/customers/by-user/{userId}")
    ResponseEntity<?> getCustomerByUserId(
            @PathVariable("userId") Long userId,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @GetMapping("/api/customers/find-id/{userId}")
    ResponseEntity<?> getCustomerIdByUserId(
            @PathVariable("userId") Long userId,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @PostMapping("/api/customers/{id}/loyalty/tier-history")
    ResponseEntity<?> createTierHistory(
            @PathVariable("id") Long customerId,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody java.util.Map<String, String> body
    );

    @PutMapping("/api/customers/{id}/loyalty/tier-sync")
    ResponseEntity<?> syncTier(
            @PathVariable("id") Long customerId,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody java.util.Map<String, String> body
    );
}
