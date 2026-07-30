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
        Long likeCount,
        Long commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PostResponse from(Post post) {
        return from(post, 0L, 0L);
    }

    public static PostResponse from(
            Post post,
            long likeCount
    ) {
        return from(post, likeCount, 0L);
    }

    public static PostResponse from(
            Post post,
            long likeCount,
            long commentCount
    ) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getStatus().name(),
                post.getViewCount(),
                likeCount,
                commentCount,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
