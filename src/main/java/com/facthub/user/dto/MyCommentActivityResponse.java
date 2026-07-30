package com.facthub.user.dto;

import com.facthub.comment.domain.Comment;

import java.time.LocalDateTime;

public record MyCommentActivityResponse(
        Long commentId,
        Long postId,
        String postTitle,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static MyCommentActivityResponse from(
            Comment comment
    ) {
        return new MyCommentActivityResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}

