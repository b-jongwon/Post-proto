package com.facthub.post.dto;

import java.util.List;

public record PostHighlightsResponse(
        List<PostSummaryResponse> popular,
        List<PostSummaryResponse> mostLiked,
        List<PostSummaryResponse> mostViewed,
        List<PostSummaryResponse> latest
) {
}

