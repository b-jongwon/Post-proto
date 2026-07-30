package com.facthub.admin.dto;

public record AdminDashboardResponse(
        long totalUserCount,
        long activeUserCount,
        long suspendedUserCount,
        long publishedPostCount,
        long hiddenPostCount,
        long totalLikeCount,
        long totalCommentCount,
        long todayPostCount,
        long todayCommentCount
) {
}

