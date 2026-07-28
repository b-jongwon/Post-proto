package com.facthub.factcheck.dto.gemini;

import com.facthub.factcheck.domain.EvidenceStance;
import com.facthub.factcheck.domain.FactCheckSourceType;

/*
 * 특정 주장을 검증하는 근거와 출처 정보
 */
public record GeminiEvidenceResult(

        /*
         * 출처 정보
         */
        String sourceTitle,

        String sourceUrl,

        String sourceSnippet,

        FactCheckSourceType sourceType,

        /*
         * 해당 출처가 주장에 수행하는 역할
         */
        EvidenceStance stance,

        /*
         * 주장을 직접 검증하는 근거 부분
         */
        String evidenceSnippet,

        /*
         * 해당 근거가 주장을 지지하거나
         * 반박하는 이유
         */
        String reasoning,

        /*
         * 주장과 근거의 관련성 점수
         * 0 이상 100 이하
         */
        Integer relevanceScore

) {
}