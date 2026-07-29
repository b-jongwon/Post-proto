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
        LocalDateTime createdAt,

        Long analysisId,
        FactCheckStatus analysisStatus,
        FactCheckVerdict analysisVerdict,
        Integer credibilityScore,
        String analysisSummary,
        LocalDateTime analysisCompletedAt,
        boolean analysisStale
) {

    /*
     * 기존 호출 코드와의 호환을 위한 기본 메서드다.
     */
    public static PostSummaryResponse from(
            Post post,
            PostAnalysisSelection selection
    ) {
        return from(
                post,
                selection,
                0L
        );
    }

    /*
     * 홈 게시글 카드에 실제 좋아요 수를 포함한다.
     */
    public static PostSummaryResponse from(
            Post post,
            PostAnalysisSelection selection,
            long likeCount
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
                likeCount,
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