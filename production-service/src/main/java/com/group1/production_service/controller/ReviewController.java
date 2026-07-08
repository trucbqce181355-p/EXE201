package com.group1.production_service.controller;

import com.group1.production_service.dto.request.CreateReviewRequest;
import com.group1.production_service.dto.response.ApiResponse;
import com.group1.production_service.dto.response.ReviewResponse;
import com.group1.production_service.security.AuthenticatedUser;
import com.group1.production_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long productId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateReviewRequest request) {
        
        ReviewResponse data = reviewService.createReview(
                productId,
                authenticatedUser.getUserId(),
                authenticatedUser.getFullName() != null ? authenticatedUser.getFullName() : authenticatedUser.getUsername(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Review submitted successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(@PathVariable Long productId) {
        List<ReviewResponse> data = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reviews fetched successfully", data));
    }
}
