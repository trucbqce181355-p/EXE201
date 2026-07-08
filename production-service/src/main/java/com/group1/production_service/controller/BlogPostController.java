package com.group1.production_service.controller;

import com.group1.production_service.dto.response.ApiResponse;
import com.group1.production_service.entity.BlogPost;
import com.group1.production_service.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class BlogPostController {

    private final BlogPostRepository blogPostRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BlogPost>>> getAllPosts() {
        List<BlogPost> posts = blogPostRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(new ApiResponse<>(true, "Posts fetched successfully", posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPost>> getPostById(@PathVariable Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return ResponseEntity.ok(new ApiResponse<>(true, "Post details fetched successfully", post));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BlogPost>> createPost(@RequestBody BlogPost request) {
        if (request.getPublishedAt() == null) {
            request.setPublishedAt(LocalDateTime.now());
        }
        BlogPost saved = blogPostRepository.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Post created successfully", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPost>> updatePost(@PathVariable Long id, @RequestBody BlogPost request) {
        BlogPost existing = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        existing.setDescription(request.getDescription());
        existing.setAuthor(request.getAuthor());
        existing.setImageUrl(request.getImageUrl());
        if (request.getPublishedAt() != null) {
            existing.setPublishedAt(request.getPublishedAt());
        }

        BlogPost saved = blogPostRepository.save(existing);
        return ResponseEntity.ok(new ApiResponse<>(true, "Post updated successfully", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        BlogPost existing = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        blogPostRepository.delete(existing);
        return ResponseEntity.ok(new ApiResponse<>(true, "Post deleted successfully", null));
    }
}
