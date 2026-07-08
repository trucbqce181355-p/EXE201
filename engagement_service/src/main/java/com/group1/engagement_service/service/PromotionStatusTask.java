package com.group1.engagement_service.service;

import com.group1.engagement_service.entity.PromotionStatus;
import com.group1.engagement_service.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionStatusTask {

    private final PromotionRepository promotionRepository;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void autoUpdatePromotionStatus() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Running auto update promotion status at: {}", now);

        int activatedCount = promotionRepository.updateStatusForStarted(now, PromotionStatus.SCHEDULED, PromotionStatus.ACTIVE);

        int expiredCount = promotionRepository.updateStatusForExpired(now, PromotionStatus.ACTIVE, PromotionStatus.EXPIRED);

        if (activatedCount > 0 || expiredCount > 0) {
            log.info("Activated: {} promotions, Expired: {} promotions", activatedCount, expiredCount);
        }
    }
}