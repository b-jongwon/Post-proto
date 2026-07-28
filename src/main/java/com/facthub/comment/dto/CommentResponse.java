package com.facthub.comment.dto;

import com.facthub.comment.domain.Comment;
import com.facthub.comment.domain.CommentStatus;

import java.time.LocalDateTime;

public record CommentResponse(

        Long commentId,
        Long postId,
        Long authorId,
        String authorNickname,
        String content,
        CommentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static CommentResponse from(
            Comment comment
    ) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getNickname(),
                comment.getContent(),
                comment.getStatus(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}