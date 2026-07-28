package com.facthub.factcheck.dto.gemini;

import com.facthub.factcheck.domain.FactCheckVerdict;

import java.util.List;

/*
 * 게시글에서 추출한 개별 핵심 주장 결과
 */
public record GeminiClaimResult(

        String claimText,

        String normalizedClaim,

        FactCheckVerdict verdict,

        Integer confidenceScore,

        String explanation,

        List<GeminiEvidenceResult> evidences

) {

    public GeminiClaimResult {
        evidences = evidences == null
                ? List.of()
                : List.copyOf(evidences);
    }
}