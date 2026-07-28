package com.facthub.factcheck.service;

import com.facthub.factcheck.exception.FactCheckException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import com.facthub.factcheck.domain.FactCheckVerdict;
import com.facthub.factcheck.dto.FactCheckResponse;
import com.facthub.factcheck.dto.gemini.GeminiFactCheckResult;
import com.facthub.factcheck.port.FactCheckAiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FactCheckServiceTest {

    private FactCheckPersistenceService
            persistenceService;

    private FactCheckAiClient
            factCheckAiClient;

    private FactCheckService
            factCheckService;

    @BeforeEach
    void setUp() {
        persistenceService =
                Mockito.mock(
                        FactCheckPersistenceService.class
                );

        factCheckAiClient =
                Mockito.mock(
                        FactCheckAiClient.class
                );

        factCheckService =
                new FactCheckService(
                        persistenceService,
                        factCheckAiClient
                );
    }

    @Test
    void analyze_success() {
        Long postId = 1L;
        Long analysisId = 10L;

        String userEmail =
                "test@facthub.com";

        String model =
                "gemini-test-model";

        FactCheckPersistenceService.FactCheckJob job =
                new FactCheckPersistenceService
                        .FactCheckJob(
                        analysisId,
                        1,
                        "테스트 제목",
                        "테스트 본문"
                );

        GeminiFactCheckResult aiResult =
                new GeminiFactCheckResult(
                        "interaction-1",
                        model,
                        FactCheckVerdict.TRUE,
                        95,
                        90,
                        "대체로 사실입니다.",
                        "근거를 확인한 결과 사실입니다.",
                        List.of()
                );

        FactCheckResponse expectedResponse =
                Mockito.mock(
                        FactCheckResponse.class
                );

        when(factCheckAiClient.getModel())
                .thenReturn(model);

        when(
                persistenceService.start(
                        postId,
                        userEmail,
                        model
                )
        ).thenReturn(job);

        when(
                factCheckAiClient.analyze(
                        job.postTitle(),
                        job.postContent()
                )
        ).thenReturn(aiResult);

        when(
                persistenceService.complete(
                        analysisId,
                        aiResult
                )
        ).thenReturn(expectedResponse);

        FactCheckResponse actualResponse =
                factCheckService.analyze(
                        postId,
                        userEmail
                );

        assertThat(actualResponse)
                .isSameAs(expectedResponse);

        verify(persistenceService)
                .start(
                        postId,
                        userEmail,
                        model
                );

        verify(factCheckAiClient)
                .analyze(
                        "테스트 제목",
                        "테스트 본문"
                );

        verify(persistenceService)
                .complete(
                        analysisId,
                        aiResult
                );
    }
    @Test
    void analyze_aiFailure_savesFailedStatus() {
        Long postId = 1L;
        Long analysisId = 10L;

        String userEmail =
                "test@facthub.com";

        String model =
                "gemini-test-model";

        String errorMessage =
                "Gemini API 호출에 실패했습니다.";

        FactCheckPersistenceService.FactCheckJob job =
                new FactCheckPersistenceService
                        .FactCheckJob(
                        analysisId,
                        1,
                        "테스트 제목",
                        "테스트 본문"
                );

        FactCheckException aiException =
                FactCheckException.upstream(
                        errorMessage
                );

        when(factCheckAiClient.getModel())
                .thenReturn(model);

        when(
                persistenceService.start(
                        postId,
                        userEmail,
                        model
                )
        ).thenReturn(job);

        when(
                factCheckAiClient.analyze(
                        job.postTitle(),
                        job.postContent()
                )
        ).thenThrow(aiException);

        assertThatThrownBy(() ->
                factCheckService.analyze(
                        postId,
                        userEmail
                )
        )
                .isSameAs(aiException)
                .hasMessage(errorMessage);

        verify(persistenceService)
                .fail(
                        analysisId,
                        errorMessage
                );

        verify(persistenceService, never())
                .complete(
                        Mockito.eq(analysisId),
                        Mockito.any()
                );
    }
}