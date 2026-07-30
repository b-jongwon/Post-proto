package com.facthub.admin.dto;

import com.facthub.post.domain.Post;
import com.facthub.post.dto.PostSummaryResponse;

import java.time.LocalDateTime;

public record AdminPostResponse(
        Long postId,
        String title,
        String category,
        Long authorId,
        String authorNickname,
        String status,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        LocalDateTime createdAt
) {

    public static AdminPostResponse from(
            Post post,
            PostSummaryResponse summary
    ) {
        return new AdminPostResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getStatus().name(),
                post.getViewCount(),
                summary.likeCount(),
                summary.commentCount(),
                post.getCreatedAt()
        );
    }
}

