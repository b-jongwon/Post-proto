package com.facthub.post.dto;

import com.facthub.factcheck.domain.FactCheckAnalysis;
import com.facthub.factcheck.domain.FactCheckStatus;
import com.facthub.factcheck.domain.FactCheckVerdict;
import com.facthub.factcheck.domain.PostAnalysisSelection;
import com.facthub.post.domain.Post;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long postId,
        String title,
        String category,
        Long authorId,
        String authorNickname,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        String contentPreview,
        LocalDateTime createdAt,

        Long analysisId,
        FactCheckStatus analysisStatus,
        FactCheckVerdict analysisVerdict,
        Integer credibilityScore,
        Integer confidenceScore,
        String analysisSummary,
        LocalDateTime analysisCompletedAt,
        boolean analysisStale
) {

    public static PostSummaryResponse from(
            Post post,
            PostAnalysisSelection selection
    ) {
        return from(
                post,
                selection,
                0L,
                0L
        );
    }

    public static PostSummaryResponse from(
            Post post,
            PostAnalysisSelection selection,
            long likeCount
    ) {
        return from(
                post,
                selection,
                likeCount,
                0L
        );
    }

    public static PostSummaryResponse from(
            Post post,
            PostAnalysisSelection selection,
            long likeCount,
            long commentCount
    ) {
        FactCheckAnalysis analysis =
                selection == null
                        ? null
                        : selection.getAnalysis();

        String normalizedContent =
                post.getContent()
                        .replaceAll("\\s+", " ")
                        .trim();

        String contentPreview =
                normalizedContent.length() <= 180
                        ? normalizedContent
                        : normalizedContent.substring(
                                0,
                                180
                        ) + "...";

        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getViewCount(),
                likeCount,
                commentCount,
                contentPreview,
                post.getCreatedAt(),

                analysis == null
                        ? null
                        : analysis.getId(),

                analysis == null
                        ? null
                        : analysis.getStatus(),

                analysis == null
                        ? null
                        : analysis.getVerdict(),

                analysis == null
                        ? null
                        : analysis.getCredibilityScore(),

                analysis == null
                        ? null
                        : analysis.getConfidenceScore(),

                analysis == null
                        ? null
                        : analysis.getSummary(),

                analysis == null
                        ? null
                        : analysis.getCompletedAt(),

                analysis != null
                        && analysis.isStale()
        );
    }
}
