package com.group1.engagement_service.controller;

import com.group1.engagement_service.dto.request.DailyPostRequest;
import com.group1.engagement_service.dto.response.DailyPostResponse;
import com.group1.engagement_service.service.DailyPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/daily-posts")
@RequiredArgsConstructor
public class DailyPostController {

    private final DailyPostService dailyPostService;

    @PostMapping
    public ResponseEntity<DailyPostResponse> createPost(@Valid @RequestBody DailyPostRequest request) {
        DailyPostResponse response = dailyPostService.createPost(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DailyPostResponse>> getAllPosts() {
        return ResponseEntity.ok(dailyPostService.getAllPosts());
    }
}
