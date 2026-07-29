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
        LocalDateTime createdAt,

        Long analysisId,
        FactCheckStatus analysisStatus,
        FactCheckVerdict analysisVerdict,
        Integer credibilityScore,
        String analysisSummary,
        LocalDateTime analysisCompletedAt,
        boolean analysisStale
) {

    public static PostSummaryResponse from(
            Post post,
            PostAnalysisSelection selection
    ) {
        FactCheckAnalysis analysis =
                selection == null
                        ? null
                        : selection.getAnalysis();

        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getViewCount(),
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
                        : analysis.getSummary(),
                analysis == null
                        ? null
                        : analysis.getCompletedAt(),
                analysis != null
                        && analysis.isStale()
        );
    }
}