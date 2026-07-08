package com.group1.engagement_service.service;

import com.group1.engagement_service.client.CustomerSegmentClient;
import com.group1.engagement_service.client.CustomerSegmentFeignClient;
import com.group1.engagement_service.dto.PreviewReachResponse;
import com.group1.engagement_service.dto.SegmentTargetInfoDto;
import com.group1.engagement_service.dto.SetTargetSegmentsRequest;
import com.group1.engagement_service.dto.TargetSegmentsResponse;
import com.group1.engagement_service.dto.external.CustomerSegmentDetailDto;
import com.group1.engagement_service.entity.Promotion;
import com.group1.engagement_service.entity.PromotionTargetMode;
import com.group1.engagement_service.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionTargetingService {

    private final PromotionRepository promotionRepository;
    private final CustomerSegmentClient segmentClient;
    private final CustomerSegmentFeignClient customerSegmentFeignClient;

    private Promotion loadPromotion(Long id) {
        return promotionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found"));
    }

    public PromotionTargetMode effectiveMode(Promotion p) {
        if (p.getTargetSegmentMode() != null) {
            return p.getTargetSegmentMode();
        }
        return PromotionTargetMode.INCLUSIVE;
    }

    public boolean hasTargetSegments(Promotion promotion) {
        return !parseSegmentIds(promotion.getTargetSegmentIds()).isEmpty();
    }

    public Set<Long> parseSegmentIds(String raw) {
        LinkedHashSet<Long> out = new LinkedHashSet<>();
        if (raw == null || raw.isBlank()) {
            return out;
        }
        for (String part : raw.split(",")) {
            String t = part.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                out.add(Long.parseLong(t));
            } catch (NumberFormatException ignored) {
                // skip invalid token
            }
        }
        return out;
    }

    private String joinSegmentIds(Set<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private long parseIdFromRequest(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid segment id: " + raw);
        }
    }

    @Transactional
    public void assignTargetSegments(Long promotionId, SetTargetSegmentsRequest request, String authorizationHeader) {
        Promotion promotion = loadPromotion(promotionId);
        Set<Long> merged = new LinkedHashSet<>(parseSegmentIds(promotion.getTargetSegmentIds()));
        for (String s : request.getSegmentIds()) {
            long sid = parseIdFromRequest(s);
            segmentClient.getSegment(sid, authorizationHeader)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Segment not found: " + sid));
            merged.add(sid);
        }
        promotion.setTargetSegmentIds(merged.isEmpty() ? null : joinSegmentIds(merged));
        promotion.setTargetSegmentMode(request.getMode());
        promotionRepository.save(promotion);
    }

    @Transactional
    public void removeTargetSegment(Long promotionId, Long segmentId) {
        Promotion promotion = loadPromotion(promotionId);
        Set<Long> ids = new LinkedHashSet<>(parseSegmentIds(promotion.getTargetSegmentIds()));
        if (!ids.remove(segmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Segment not linked to this promotion");
        }
        if (ids.isEmpty()) {
            promotion.setTargetSegmentIds(null);
            promotion.setTargetSegmentMode(null);
        } else {
            promotion.setTargetSegmentIds(joinSegmentIds(ids));
        }
        promotionRepository.save(promotion);
    }

    public TargetSegmentsResponse getTargetSegments(Long promotionId, String authorizationHeader) {
        Promotion promotion = loadPromotion(promotionId);
        Set<Long> ids = parseSegmentIds(promotion.getTargetSegmentIds());
        List<SegmentTargetInfoDto> segments = new ArrayList<>();
        for (Long id : ids) {
            segmentClient.getSegment(id, authorizationHeader).ifPresentOrElse(
                    d -> segments.add(toInfo(d)),
                    () -> segments.add(SegmentTargetInfoDto.builder()
                            .segment_id(id)
                            .name("(unavailable)")
                            .description("Could not load segment from customer-service")
                            .customer_count(null)
                            .build())
            );
        }
        return TargetSegmentsResponse.builder()
                .mode(promotion.getTargetSegmentMode())
                .segment_ids(new ArrayList<>(ids))
                .segments(segments)
                .build();
    }

    public PreviewReachResponse previewReach(Long promotionId, String authorizationHeader) {

        Promotion promotion = loadPromotion(promotionId);
        PromotionTargetMode mode = effectiveMode(promotion);
        Set<Long> ids = parseSegmentIds(promotion.getTargetSegmentIds());

        Long total;
        String note;

        // Không có segment → apply all
        if (ids.isEmpty()) {
            total = null; // hoặc gọi API count all nếu bạn muốn
            note = "No segments targeted — promotion applies to all customers.";
        } else {
            // 🔥 CALL 1 LẦN DUY NHẤT
            total = segmentClient.countCustomersBySegments(
                    new ArrayList<>(ids),
                    mode.name(),
                    authorizationHeader
            );

            if (mode == PromotionTargetMode.INCLUSIVE) {
                note = "INCLUSIVE: total customers in selected segments (distinct).";
            } else {
                note = "EXCLUSIVE: total customers excluding selected segments.";
            }
        }
        return PreviewReachResponse.builder()
                .total_customers(total)
                .mode(mode)
                .segments(Collections.emptyList()) // nếu không cần chi tiết segment
                .counting_note(note)
                .build();
    }

    private static SegmentTargetInfoDto toInfo(CustomerSegmentDetailDto d) {
        return SegmentTargetInfoDto.builder()
                .segment_id(d.getId())
                .name(d.getName())
                .description(d.getDescription())
                .customer_count(d.getCustomerCount())
                .build();
    }

    /**
     * Danh sách khuyến mãi "Đang áp dụng" khi user đã đăng nhập: chỉ promotion đã gán ít nhất một segment
     * và khách đủ điều kiện theo {@link #isCustomerEligible}. Promotion chưa gán segment không hiển thị.
     * (Coupon / apply vẫn dùng {@link #isCustomerEligible} — chưa gán segment = áp dụng mọi khách.)
     */
    public boolean isEligibleForAuthenticatedAvailableList(
            Promotion promotion, Long customerId, String authorizationHeader) {
        Set<Long> ids = parseSegmentIds(promotion.getTargetSegmentIds());
        if (ids.isEmpty()) {
            return false;
        }
        return isCustomerEligible(promotion, customerId, authorizationHeader);
    }

    /**
     * AC 29.4 — segment gate for coupon / promotion apply.
     */
    public boolean isCustomerEligible(Promotion promotion, Long customerId, String authorizationHeader) {
        Set<Long> ids = parseSegmentIds(promotion.getTargetSegmentIds());

        if (ids.isEmpty()) {
            return true;
        }

        if (customerId == null || authorizationHeader == null || authorizationHeader.isBlank()) {
            return false;
        }

        boolean isMember = segmentClient.isCustomerMemberOfSegments(
                new ArrayList<>(ids),
                customerId,
                authorizationHeader
        );

        PromotionTargetMode mode = effectiveMode(promotion);

        if (mode == PromotionTargetMode.INCLUSIVE) {
            return isMember;
        } else {
            return !isMember;
        }
    }
}
