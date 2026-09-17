package com.group1.engagement_service.repository;

import com.group1.engagement_service.entity.DailyPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyPostRepository extends JpaRepository<DailyPost, Long> {
}
