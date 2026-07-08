package com.group1.engagement_service.client;

import com.group1.engagement_service.dto.external.CustomerSegmentDetailDto;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Delegates to {@link CustomerSegmentFeignClient}; keeps the same tolerance as the former RestTemplate client (empty on errors).
 */
@Component
public class CustomerSegmentClient {

    private final CustomerSegmentFeignClient feign;

    public CustomerSegmentClient(CustomerSegmentFeignClient feign) {
        this.feign = feign;
    }

    private static String normalizeAuthorization(String authorizationHeaderValue) {
        if (authorizationHeaderValue == null || authorizationHeaderValue.isBlank()) {
            return null;
        }
        String v = authorizationHeaderValue.trim();
        return v.startsWith("Bearer ") ? v : "Bearer " + v;
    }

    public Optional<CustomerSegmentDetailDto> getSegment(long segmentId, String authorizationHeaderValue) {
        try {
            CustomerSegmentDetailDto body = feign.getSegment(segmentId, normalizeAuthorization(authorizationHeaderValue));
            return Optional.ofNullable(body);
        } catch (FeignException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCustomersPage(long segmentId, int page, int limit, String authorizationHeaderValue) {
        try {
            Map<String, Object> body = feign.getCustomersInSegment(segmentId, page, limit, normalizeAuthorization(authorizationHeaderValue));
            if (body == null || !body.containsKey("data")) {
                return Collections.emptyList();
            }
            Object data = body.get("data");
            if (!(data instanceof List<?> list)) {
                return Collections.emptyList();
            }
            return (List<Map<String, Object>>) list;
        } catch (FeignException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Gọi {@code GET /customer-segments/{id}/membership/{customerId}} — khách hàng thường không có SEGMENT:READ
     * nên không dùng {@link #getCustomersPage} cho eligibility.
     */
    public boolean isCustomerMemberOfSegments(List<Long> segmentIds, Long customerId, String authorizationHeaderValue) {
        try {
            Map<String, Object> body = feign.getSegmentMembership(
                    segmentIds,
                    customerId,
                    normalizeAuthorization(authorizationHeaderValue)
            );

            if (body == null || !body.containsKey("member")) {
                return false;
            }

            Object m = body.get("member");
            if (m instanceof Boolean b) {
                return b;
            }

            return Boolean.parseBoolean(String.valueOf(m));
        } catch (FeignException e) {
            return false;
        }
    }

    public long countCustomersBySegments(List<Long> segmentIds, String mode, String authorizationHeaderValue) {
        try {
            Map<String, Long> body = feign.countCustomersBySegments(
                    segmentIds,
                    mode,
                    normalizeAuthorization(authorizationHeaderValue)
            );

            if (body == null || !body.containsKey("count")) {
                return 0L;
            }

            return body.getOrDefault("count", 0L);

        } catch (FeignException e) {
            return 0L;
        }
    }
}
