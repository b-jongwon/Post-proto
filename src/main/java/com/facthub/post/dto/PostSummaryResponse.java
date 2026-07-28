package com.facthub.post.dto;

import com.facthub.post.domain.Post;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long postId,
        String title,
        String category,
        Long authorId,
        String authorNickname,
        Long viewCount,
        LocalDateTime createdAt
) {

    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}