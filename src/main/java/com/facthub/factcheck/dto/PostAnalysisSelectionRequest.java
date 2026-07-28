package com.facthub.factcheck.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PostAnalysisSelectionRequest(

        @NotNull(
                message = "대표 분석 ID는 필수입니다."
        )
        @Positive(
                message = "대표 분석 ID는 1 이상이어야 합니다."
        )
        Long analysisId

) {
}