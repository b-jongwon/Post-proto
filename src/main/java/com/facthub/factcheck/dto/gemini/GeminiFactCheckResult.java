package com.facthub.factcheck.dto.gemini;

import com.facthub.factcheck.domain.FactCheckVerdict;

import java.util.List;

/*
 * Gemini 팩트체크 전체 결과
 */
public record GeminiFactCheckResult(

        String interactionId,

        String model,

        FactCheckVerdict verdict,

        Integer credibilityScore,

        Integer confidenceScore,

        String summary,

        String explanation,

        List<GeminiClaimResult> claims

) {

    public GeminiFactCheckResult {
        claims = claims == null
                ? List.of()
                : List.copyOf(claims);
    }
}