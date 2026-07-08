package com.group1.production_service.service;

import com.group1.production_service.dto.request.CreateReviewRequest;
import com.group1.production_service.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(Long productId, Long userId, String userName, CreateReviewRequest request);
    List<ReviewResponse> getReviewsByProduct(Long productId);
}
