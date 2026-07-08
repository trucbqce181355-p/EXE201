package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.PromotionListItemDto;
import com.group1.engagement_service.entity.Promotion;
import com.group1.engagement_service.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionManagementService {

    private final PromotionRepository promotionRepository;

    @Transactional(readOnly = true)
    public List<PromotionListItemDto> listNonDeleted() {
        return promotionRepository.findByIsDeletedFalseOrderByIdDesc().stream()
                .map(this::toListItem)
                .toList();
    }

    private PromotionListItemDto toListItem(Promotion p) {
        return PromotionListItemDto.builder()
                .id(p.getId())
                .name(p.getName())
                .status(p.getStatus())
                .type(p.getType())
                .value(p.getValue())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .targetSegmentIds(p.getTargetSegmentIds())
                .targetSegmentMode(p.getTargetSegmentMode())
                .build();
    }
}
