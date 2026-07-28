package com.facthub.factcheck.port;

import com.facthub.factcheck.dto.gemini.GeminiFactCheckResult;

public interface FactCheckAiClient {

    /*
     * 게시글 제목과 본문을 외부 AI로 분석한다.
     */
    GeminiFactCheckResult analyze(
            String postTitle,
            String postContent
    );

    /*
     * 분석에 사용되는 외부 AI 모델명을 반환한다.
     */
    String getModel();
}