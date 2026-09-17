package com.group1.engagement_service.service;

import com.group1.engagement_service.dto.request.DailyPostRequest;
import com.group1.engagement_service.dto.response.DailyPostResponse;
import com.group1.engagement_service.entity.DailyPost;
import com.group1.engagement_service.repository.DailyPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyPostService {

    private final DailyPostRepository dailyPostRepository;

    public DailyPostResponse createPost(DailyPostRequest request) {
        DailyPost dailyPost = DailyPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .avatarUrl(request.getAvatarUrl())
                .build();
        
        DailyPost savedPost = dailyPostRepository.save(dailyPost);
        return mapToResponse(savedPost);
    }

    public List<DailyPostResponse> getAllPosts() {
        return dailyPostRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DailyPostResponse mapToResponse(DailyPost post) {
        return DailyPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .avatarUrl(post.getAvatarUrl())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
