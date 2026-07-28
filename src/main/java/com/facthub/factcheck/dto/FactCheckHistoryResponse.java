package com.facthub.factcheck.dto;

import com.facthub.factcheck.domain.FactCheckAnalysis;
import com.facthub.factcheck.domain.FactCheckStatus;
import com.facthub.factcheck.domain.FactCheckVerdict;

import java.time.LocalDateTime;

public record FactCheckHistoryResponse(

        Long analysisId,
        Long postId,
        Integer runNumber,

        FactCheckStatus status,
        FactCheckVerdict verdict,

        Integer credibilityScore,
        Integer confidenceScore,

        String summary,
        String model,

        String promptVersion,
        String schemaVersion,

        boolean isStale,
        boolean isSelected,

        String errorMessage,

        Long requestedByUserId,
        String requestedByNickname,

        LocalDateTime createdAt,
        LocalDateTime completedAt

) {

    public static FactCheckHistoryResponse from(
            FactCheckAnalysis analysis,
            boolean selected
    ) {
        return new FactCheckHistoryResponse(
                analysis.getId(),
                analysis.getPost().getId(),
                analysis.getRunNumber(),

                analysis.getStatus(),
                analysis.getVerdict(),

                analysis.getCredibilityScore(),
                analysis.getConfidenceScore(),

                analysis.getSummary(),
                analysis.getModel(),

                analysis.getPromptVersion(),
                analysis.getSchemaVersion(),

                analysis.isStale(),
                selected,

                analysis.getErrorMessage(),

                analysis.getRequestedBy().getId(),
                analysis.getRequestedBy().getNickname(),

                analysis.getCreatedAt(),
                analysis.getCompletedAt()
        );
    }
}