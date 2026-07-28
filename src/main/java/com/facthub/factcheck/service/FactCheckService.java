package com.facthub.factcheck.service;

import com.facthub.factcheck.port.FactCheckAiClient;
import com.facthub.factcheck.dto.FactCheckHistoryResponse;
import com.facthub.factcheck.dto.FactCheckResponse;
import com.facthub.factcheck.dto.gemini.GeminiFactCheckResult;
import com.facthub.factcheck.exception.FactCheckException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FactCheckService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    FactCheckService.class
            );

    private final FactCheckPersistenceService
            persistenceService;

    private final FactCheckAiClient
            factCheckAiClient;

    public FactCheckService(
            FactCheckPersistenceService
                    persistenceService,

            FactCheckAiClient
                    factCheckAiClient
    ) {
        this.persistenceService =
                persistenceService;

        this.factCheckAiClient =
                factCheckAiClient;
    }

    /*
     * 새로운 분석 실행
     *
     * 이 메서드에는 @Transactional을 붙이지 않는다.
     *
     * Gemini API 호출 시간 동안
     * DB 트랜잭션을 유지하지 않기 위해서다.
     */
    public FactCheckResponse analyze(
            Long postId,
            String userEmail
    ) {
        FactCheckPersistenceService.FactCheckJob job =
                persistenceService.start(
                        postId,
                        userEmail,
                        factCheckAiClient.getModel()
                );

        try {
            GeminiFactCheckResult result =
                    factCheckAiClient.analyze(
                            job.postTitle(),
                            job.postContent()
                    );

            return persistenceService.complete(
                    job.analysisId(),
                    result
            );

        } catch (FactCheckException exception) {

            persistenceService.fail(
                    job.analysisId(),
                    exception.getMessage()
            );

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "예상하지 못한 팩트체크 오류가 발생했습니다. analysisId={}",
                    job.analysisId(),
                    exception
            );

            persistenceService.fail(
                    job.analysisId(),
                    "예상하지 못한 분석 오류가 발생했습니다."
            );

            throw FactCheckException.upstream(
                    "팩트체크 처리 중 오류가 발생했습니다."
            );
        }
    }

    /*
     * 현재 대표 분석 조회
     */
    public FactCheckResponse getAnalysis(
            Long postId
    ) {
        return persistenceService
                .getRepresentative(postId);
    }

    /*
     * 게시글의 전체 분석 이력 조회
     */
    public List<FactCheckHistoryResponse> getHistory(
            Long postId
    ) {
        return persistenceService
                .getHistory(postId);
    }

    /*
     * 게시글에 속한 특정 분석 상세 조회
     */
    public FactCheckResponse getDetail(
            Long postId,
            Long analysisId
    ) {
        return persistenceService
                .getDetail(
                        postId,
                        analysisId
                );
    }

    /*
     * 게시글의 대표 분석 변경
     */
    public FactCheckResponse changeRepresentative(
            Long postId,
            Long analysisId,
            String userEmail
    ) {
        return persistenceService
                .changeRepresentative(
                        postId,
                        analysisId,
                        userEmail
                );
    }
}