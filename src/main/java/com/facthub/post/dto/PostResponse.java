package com.facthub.post.dto;

import com.facthub.post.domain.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long postId,
        String title,
        String content,
        String category,
        Long authorId,
        String authorNickname,
        String status,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getStatus().name(),
                post.getViewCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}