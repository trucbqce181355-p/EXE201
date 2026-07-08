package com.group1.engagement_service.entity;

/**
 * How {@link Promotion#getTargetSegmentIds()} restricts eligibility.
 */
public enum PromotionTargetMode {
    /** Only customers in at least one targeted segment may use the promotion. */
    INCLUSIVE,
    /** All customers may use the promotion except those in any targeted segment. */
    EXCLUSIVE
}
