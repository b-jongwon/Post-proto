package com.facthub.post.dto;

public record PostStatisticsResponse(
        long totalPostCount,
        long completedVerificationCount,
        long pendingVerificationCount
) {

    public static PostStatisticsResponse of(
            long totalPostCount,
            long completedVerificationCount
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
                totalPostCount,
                safeCompletedCount,
                pendingVerificationCount
        );
    }
}