package com.facthub.postlike.dto;

public record PostLikeResponse(

        Long postId,
        long likeCount,
        boolean liked

) {
}