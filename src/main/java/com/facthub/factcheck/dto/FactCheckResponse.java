package com.facthub.factcheck.dto;

import com.facthub.factcheck.domain.EvidenceStance;
import com.facthub.factcheck.domain.FactCheckAnalysis;
import com.facthub.factcheck.domain.FactCheckClaim;
import com.facthub.factcheck.domain.FactCheckEvidence;
import com.facthub.factcheck.domain.FactCheckSource;
import com.facthub.factcheck.domain.FactCheckSourceType;
import com.facthub.factcheck.domain.FactCheckStatus;
import com.facthub.factcheck.domain.FactCheckVerdict;

import java.time.LocalDateTime;
import java.util.List;

public record FactCheckResponse(

        Long analysisId,
        Long postId,
        Integer runNumber,

        FactCheckStatus status,
        FactCheckVerdict verdict,

        Integer credibilityScore,
        Integer confidenceScore,

        String summary,
        String explanation,

        String model,
        String interactionId,
        String promptVersion,
        String schemaVersion,

        String postTitleSnapshot,
        String postContentSnapshot,
        String postContentHash,

        boolean isStale,
        String errorMessage,

        Long requestedByUserId,
        String requestedByNickname,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,

        List<SourceResponse> sources,
        List<ClaimResponse> claims,

        String disclaimer

) {

    private static final String DISCLAIMER =
            "AI 분석 결과는 참고 자료이며 최종적인 사실 판정을 보장하지 않습니다.";

    public static FactCheckResponse from(
            FactCheckAnalysis analysis
    ) {
        List<SourceResponse> sourceResponses =
                analysis.getSources()
                        .stream()
                        .map(SourceResponse::from)
                        .toList();

        List<ClaimResponse> claimResponses =
                analysis.getClaims()
                        .stream()
                        .map(ClaimResponse::from)
                        .toList();

        return new FactCheckResponse(
                analysis.getId(),
                analysis.getPost().getId(),
                analysis.getRunNumber(),

                analysis.getStatus(),
                analysis.getVerdict(),

                analysis.getCredibilityScore(),
                analysis.getConfidenceScore(),

                analysis.getSummary(),
                analysis.getExplanation(),

                analysis.getModel(),
                analysis.getInteractionId(),
                analysis.getPromptVersion(),
                analysis.getSchemaVersion(),

                analysis.getPostTitleSnapshot(),
                analysis.getPostContentSnapshot(),
                analysis.getPostContentHash(),

                analysis.isStale(),
                analysis.getErrorMessage(),

                analysis.getRequestedBy().getId(),
                analysis.getRequestedBy().getNickname(),

                analysis.getCreatedAt(),
                analysis.getUpdatedAt(),
                analysis.getCompletedAt(),

                sourceResponses,
                claimResponses,

                DISCLAIMER
        );
    }

    /*
     * 분석 전체에서 사용된 출처
     */
    public record SourceResponse(

            Long sourceId,
            Integer sourceOrder,

            String title,
            String url,
            String canonicalUrl,
            String domain,

            FactCheckSourceType sourceType,
            String snippet,

            LocalDateTime publishedAt,
            LocalDateTime retrievedAt

    ) {

        public static SourceResponse from(
                FactCheckSource source
        ) {
            return new SourceResponse(
                    source.getId(),
                    source.getSourceOrder(),

                    source.getTitle(),
                    source.getUrl(),
                    source.getCanonicalUrl(),
                    source.getDomain(),

                    source.getSourceType(),
                    source.getSnippet(),

                    source.getPublishedAt(),
                    source.getRetrievedAt()
            );
        }
    }

    /*
     * 분석에서 추출된 개별 주장
     */
    public record ClaimResponse(

            Long claimId,
            Integer claimOrder,

            String claimText,
            String normalizedClaim,

            FactCheckVerdict verdict,
            Integer confidenceScore,

            String explanation,
            LocalDateTime createdAt,

            List<EvidenceResponse> evidences

    ) {

        public static ClaimResponse from(
                FactCheckClaim claim
        ) {
            List<EvidenceResponse> evidenceResponses =
                    claim.getEvidences()
                            .stream()
                            .map(EvidenceResponse::from)
                            .toList();

            return new ClaimResponse(
                    claim.getId(),
                    claim.getClaimOrder(),

                    claim.getClaimText(),
                    claim.getNormalizedClaim(),

                    claim.getVerdict(),
                    claim.getConfidenceScore(),

                    claim.getExplanation(),
                    claim.getCreatedAt(),

                    evidenceResponses
            );
        }
    }

    /*
     * 특정 주장을 검증하는 개별 근거
     */
    public record EvidenceResponse(

            Long evidenceId,
            Integer evidenceOrder,

            EvidenceStance stance,
            String snippet,
            String reasoning,
            Integer relevanceScore,

            LocalDateTime createdAt,

            SourceResponse source

    ) {

        public static EvidenceResponse from(
                FactCheckEvidence evidence
        ) {
            return new EvidenceResponse(
                    evidence.getId(),
                    evidence.getEvidenceOrder(),

                    evidence.getStance(),
                    evidence.getSnippet(),
                    evidence.getReasoning(),
                    evidence.getRelevanceScore(),

                    evidence.getCreatedAt(),

                    SourceResponse.from(
                            evidence.getSource()
                    )
            );
        }
    }
}