package com.facthub.post.dto;

public record PostStatisticsResponse(
        long totalPostCount,
        long totalLikeCount,
        long totalCommentCount,
        long todayPostCount,
        long completedVerificationCount,
        long pendingVerificationCount
) {

    public static PostStatisticsResponse of(
            long totalPostCount,
            long completedVerificationCount,
            long totalLikeCount,
            long totalCommentCount,
            long todayPostCount
    ) {
        long safeCompletedCount = Math.max(
                0L,
                Math.min(
                        completedVerificationCount,
                        totalPostCount
                )
        );

        long pendingVerificationCount =
                Math.max(
                        0L,
                        totalPostCount
                                - safeCompletedCount
                );

        return new PostStatisticsResponse(
                Math.max(0L, totalPostCount),
                Math.max(0L, totalLikeCount),
                Math.max(0L, totalCommentCount),
                Math.max(0L, todayPostCount),
                safeCompletedCount,
                pendingVerificationCount
        );
    }
}
