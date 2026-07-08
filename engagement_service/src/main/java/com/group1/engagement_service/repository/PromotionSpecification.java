package com.group1.engagement_service.repository;

import com.group1.engagement_service.entity.Promotion;
import com.group1.engagement_service.entity.PromotionStatus;
import com.group1.engagement_service.entity.PromotionType;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class PromotionSpecification {

    public static Specification<Promotion> filterPromotions(PromotionStatus status, PromotionType type) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}