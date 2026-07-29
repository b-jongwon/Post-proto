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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /*
     * 새 게시글처럼 좋아요가 확실히 0개인 경우를 위한 메서드다.
     */
    public static PostResponse from(Post post) {
        return from(post, 0L);
    }

    /*
     * 게시글 상세 조회, 수정 응답 등에
     * 실제 좋아요 수를 포함한다.
     */
    public static PostResponse from(
            Post post,
            long likeCount
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
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}