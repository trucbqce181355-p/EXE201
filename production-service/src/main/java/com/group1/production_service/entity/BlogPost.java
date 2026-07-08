package com.group1.production_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BlogPost extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String author;

    private String imageUrl;

    private LocalDateTime publishedAt;
}
