package com.facthub.user.dto;

import com.facthub.post.dto.PostSummaryResponse;

import java.util.List;

public record MyDashboardResponse(
        MyInfoResponse profile,
        long postCount,
        long commentCount,
        long likedPostCount,
        long unreadNotificationCount,
        List<PostSummaryResponse> posts,
        List<MyCommentActivityResponse> comments,
        List<PostSummaryResponse> likedPosts
) {
}

