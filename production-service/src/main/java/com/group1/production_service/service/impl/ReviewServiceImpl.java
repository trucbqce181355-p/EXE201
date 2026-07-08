package com.group1.production_service.service.impl;

import com.group1.production_service.dto.request.CreateReviewRequest;
import com.group1.production_service.dto.response.ReviewResponse;
import com.group1.production_service.entity.Product;
import com.group1.production_service.entity.Review;
import com.group1.production_service.exception.ResourceNotFoundException;
import com.group1.production_service.repository.ProductRepository;
import com.group1.production_service.repository.ReviewRepository;
import com.group1.production_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(Long productId, Long userId, String userName, CreateReviewRequest request) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Review review = Review.builder()
                .product(product)
                .userId(userId)
                .userName(userName)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        return mapToResponse(savedReview);
    }

    @Override
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
