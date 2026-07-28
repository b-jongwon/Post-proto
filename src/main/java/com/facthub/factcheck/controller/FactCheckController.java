package com.facthub.factcheck.controller;

import com.facthub.common.response.ApiResponse;
import com.facthub.factcheck.dto.FactCheckHistoryResponse;
import com.facthub.factcheck.dto.FactCheckResponse;
import com.facthub.factcheck.dto.PostAnalysisSelectionRequest;
import com.facthub.factcheck.service.FactCheckService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}")
public class FactCheckController {

    private final FactCheckService factCheckService;

    public FactCheckController(
            FactCheckService factCheckService
    ) {
        this.factCheckService =
                factCheckService;
    }

    /*
     * 새로운 팩트체크 분석 실행
     *
     * 게시글 작성자 또는 관리자만 가능하다.
     * 로그인 세션과 CSRF 토큰이 필요하다.
     */
    @PostMapping("/analyses")
    public ApiResponse<FactCheckResponse> analyze(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        FactCheckResponse response =
                factCheckService.analyze(
                        postId,
                        authentication.getName()
                );

        return ApiResponse.success(response);
    }

    /*
     * 게시글의 전체 분석 이력 조회
     *
     * 가장 최근 분석부터 반환한다.
     * 대표 분석은 isSelected=true로 표시된다.
     */
    @GetMapping("/analyses")
    public ApiResponse<List<FactCheckHistoryResponse>>
    getHistory(
            @PathVariable Long postId
    ) {
        List<FactCheckHistoryResponse> response =
                factCheckService.getHistory(
                        postId
                );

        return ApiResponse.success(response);
    }

    /*
     * 게시글에 속한 특정 분석 상세 조회
     *
     * claims, evidences, sources를 포함한다.
     */
    @GetMapping("/analyses/{analysisId}")
    public ApiResponse<FactCheckResponse> getDetail(
            @PathVariable Long postId,
            @PathVariable Long analysisId
    ) {
        FactCheckResponse response =
                factCheckService.getDetail(
                        postId,
                        analysisId
                );

        return ApiResponse.success(response);
    }

    /*
     * 게시글의 현재 대표 분석 조회
     */
    @GetMapping("/analysis")
    public ApiResponse<FactCheckResponse>
    getRepresentativeAnalysis(
            @PathVariable Long postId
    ) {
        FactCheckResponse response =
                factCheckService.getAnalysis(
                        postId
                );

        return ApiResponse.success(response);
    }

    /*
     * 게시글의 대표 분석 변경
     *
     * 게시글 작성자 또는 관리자만 가능하다.
     * 로그인 세션과 CSRF 토큰이 필요하다.
     */
    @PutMapping("/analysis-selection")
    public ApiResponse<FactCheckResponse>
    changeRepresentativeAnalysis(
            @PathVariable Long postId,

            @Valid
            @RequestBody
            PostAnalysisSelectionRequest request,

            Authentication authentication
    ) {
        FactCheckResponse response =
                factCheckService
                        .changeRepresentative(
                                postId,
                                request.analysisId(),
                                authentication.getName()
                        );

        return ApiResponse.success(response);
    }
}