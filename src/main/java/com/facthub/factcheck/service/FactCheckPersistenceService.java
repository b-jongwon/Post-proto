package com.facthub.factcheck.service;

import com.facthub.factcheck.dto.FactCheckHistoryResponse;

import java.util.List;

import com.facthub.common.exception.PostAccessDeniedException;
import com.facthub.common.exception.PostNotFoundException;
import com.facthub.factcheck.domain.FactCheckAnalysis;
import com.facthub.factcheck.domain.FactCheckClaim;
import com.facthub.factcheck.domain.FactCheckSource;
import com.facthub.factcheck.domain.FactCheckStatus;
import com.facthub.factcheck.domain.PostAnalysisSelection;
import com.facthub.factcheck.dto.FactCheckResponse;
import com.facthub.factcheck.dto.gemini.GeminiClaimResult;
import com.facthub.factcheck.dto.gemini.GeminiEvidenceResult;
import com.facthub.factcheck.dto.gemini.GeminiFactCheckResult;
import com.facthub.factcheck.exception.FactCheckException;
import com.facthub.factcheck.repository.FactCheckAnalysisRepository;
import com.facthub.factcheck.repository.PostAnalysisSelectionRepository;
import com.facthub.post.domain.Post;
import com.facthub.post.domain.PostStatus;
import com.facthub.post.repository.PostRepository;
import com.facthub.user.domain.User;
import com.facthub.user.domain.UserRole;
import com.facthub.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class FactCheckPersistenceService {

    /*
     * 프롬프트와 Gemini JSON 구조가 변경될 때
     * 이 값을 올려서 과거 결과와 구분한다.
     */
    private static final String PROMPT_VERSION =
            "claims-v1";

    private static final String SCHEMA_VERSION =
            "claims-v1";

    private final FactCheckAnalysisRepository
            analysisRepository;

    private final PostAnalysisSelectionRepository
            selectionRepository;

    private final PostRepository postRepository;
    private final UserService userService;

    public FactCheckPersistenceService(
            FactCheckAnalysisRepository
                    analysisRepository,

            PostAnalysisSelectionRepository
                    selectionRepository,

            PostRepository postRepository,
            UserService userService
    ) {
        this.analysisRepository =
                analysisRepository;

        this.selectionRepository =
                selectionRepository;

        this.postRepository = postRepository;
        this.userService = userService;
    }

    /*
     * 새로운 팩트체크 실행 행 생성
     *
     * 이 메서드는 짧은 트랜잭션 안에서:
     *
     * 1. 게시글 잠금
     * 2. 권한 검사
     * 3. 동시 분석 검사
     * 4. runNumber 계산
     * 5. PROCESSING 분석 저장
     *
     * 까지만 처리한다.
     *
     * 실제 Gemini API 호출은
     * 이 트랜잭션이 끝난 후 수행된다.
     */
    @Transactional
    public FactCheckJob start(
            Long postId,
            String userEmail,
            String model
    ) {
        Post post = postRepository
                .findByIdAndStatusForUpdate(
                        postId,
                        PostStatus.PUBLISHED
                )
                .orElseThrow(
                        PostNotFoundException::new
                );

        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        validateRequestPermission(
                post,
                user
        );

        boolean alreadyRunning =
                analysisRepository
                        .existsByPost_IdAndStatus(
                                postId,
                                FactCheckStatus.PROCESSING
                        );

        if (alreadyRunning) {
            throw FactCheckException
                    .alreadyRunning();
        }

        Integer maxRunNumber =
                analysisRepository
                        .findMaxRunNumberByPostId(
                                postId
                        );

        Integer newRunNumber =
                maxRunNumber + 1;

        String titleSnapshot =
                post.getTitle();

        String contentSnapshot =
                post.getContent();

        String contentHash =
                createSha256(
                        titleSnapshot
                                + "\n"
                                + contentSnapshot
                );

        FactCheckAnalysis analysis =
                FactCheckAnalysis.start(
                        post,
                        newRunNumber,
                        user,
                        limit(model, 100),
                        PROMPT_VERSION,
                        SCHEMA_VERSION,
                        titleSnapshot,
                        contentSnapshot,
                        contentHash
                );

        FactCheckAnalysis saved =
                analysisRepository
                        .saveAndFlush(analysis);

        return new FactCheckJob(
                saved.getId(),
                saved.getRunNumber(),
                titleSnapshot,
                contentSnapshot
        );
    }

    /*
     * Gemini 성공 결과 저장
     *
     * 저장 순서:
     *
     * Analysis 전체 결과
     * → Claim
     * → Source 중복 제거
     * → Evidence로 Claim과 Source 연결
     * → 대표 분석 지정
     */
    @Transactional
    public FactCheckResponse complete(
            Long analysisId,
            GeminiFactCheckResult result
    ) {
        FactCheckAnalysis analysis =
                analysisRepository
                        .findById(analysisId)
                        .orElseThrow(
                                FactCheckException::notFound
                        );

        analysis.complete(
                result.verdict(),
                result.credibilityScore(),
                result.confidenceScore(),
                limit(
                        result.summary(),
                        1000
                ),
                result.explanation(),
                limit(
                        result.interactionId(),
                        255
                ),
                limit(
                        result.model(),
                        100
                )
        );

        /*
         * 같은 분석 안에서 동일 URL 출처는
         * 한 번만 생성한다.
         *
         * key: URL의 SHA-256 해시
         * value: 생성된 Source Entity
         */
        Map<String, FactCheckSource> sourceMap =
                new LinkedHashMap<>();

        int claimOrder = 1;

        for (GeminiClaimResult claimResult
                : result.claims()) {

            if (claimResult == null) {
                continue;
            }

            FactCheckClaim claim =
                    analysis.addClaim(
                            claimOrder++,
                            normalizeRequired(
                                    claimResult
                                            .claimText()
                            ),
                            normalizeRequired(
                                    claimResult
                                            .normalizedClaim()
                            ),
                            claimResult.verdict(),
                            claimResult
                                    .confidenceScore(),
                            normalizeRequired(
                                    claimResult
                                            .explanation()
                            )
                    );

            int evidenceOrder = 1;

            for (GeminiEvidenceResult evidenceResult
                    : claimResult.evidences()) {

                if (evidenceResult == null) {
                    continue;
                }

                String sourceUrl =
                        normalizeRequired(
                                evidenceResult
                                        .sourceUrl()
                        );

                String urlHash =
                        createSha256(sourceUrl);

                FactCheckSource source =
                        sourceMap.get(urlHash);

                if (source == null) {

                    int sourceOrder =
                            sourceMap.size() + 1;

                    source =
                            analysis.addSource(
                                    sourceOrder,
                                    limit(
                                            normalizeRequired(
                                                    evidenceResult
                                                            .sourceTitle()
                                            ),
                                            500
                                    ),
                                    limit(
                                            sourceUrl,
                                            2048
                                    ),

                                    /*
                                     * 현재는 Grounding 리다이렉트
                                     * 원본 URL을 별도로 해석하지 않으므로
                                     * canonicalUrl에도 동일 URL을 저장한다.
                                     */
                                    limit(
                                            sourceUrl,
                                            2048
                                    ),

                                    urlHash,
                                    limit(
                                            extractDomain(
                                                    sourceUrl
                                            ),
                                            255
                                    ),
                                    evidenceResult
                                            .sourceType(),
                                    limit(
                                            normalizeOptional(
                                                    evidenceResult
                                                            .sourceSnippet()
                                            ),
                                            2000
                                    ),
                                    null,
                                    LocalDateTime.now()
                            );

                    sourceMap.put(
                            urlHash,
                            source
                    );
                }

                claim.addEvidence(
                        source,
                        evidenceOrder++,
                        evidenceResult.stance(),
                        normalizeOptional(
                                evidenceResult
                                        .evidenceSnippet()
                        ),
                        normalizeRequired(
                                evidenceResult
                                        .reasoning()
                        ),
                        evidenceResult
                                .relevanceScore()
                );
            }
        }

        /*
         * Analysis가 관리 상태이므로
         * CascadeType.ALL에 의해
         * Claim, Source, Evidence도 함께 저장된다.
         */
        analysisRepository.saveAndFlush(
                analysis
        );

        updateRepresentativeAnalysis(
                analysis
        );

        return FactCheckResponse.from(
                analysis
        );
    }

    /*
     * 분석 실패 상태 저장
     */
    @Transactional
    public void fail(
            Long analysisId,
            String errorMessage
    ) {
        analysisRepository
                .findById(analysisId)
                .ifPresent(analysis ->
                        analysis.fail(
                                limit(
                                        errorMessage,
                                        1000
                                )
                        )
                );
    }

    /*
     * 현재 대표 분석 조회
     */
    @Transactional(readOnly = true)
    public FactCheckResponse getRepresentative(
            Long postId
    ) {
        findPublishedPost(postId);

        PostAnalysisSelection selection =
                selectionRepository
                        .findByPost_Id(postId)
                        .orElseThrow(
                                FactCheckException::notFound
                        );

        return FactCheckResponse.from(
                selection.getAnalysis()
        );
    }

    /*
     * 게시글의 전체 분석 이력 조회
     *
     * 가장 최근 분석부터 반환한다.
     * 현재 대표 분석은 isSelected=true로 표시한다.
     */
    @Transactional(readOnly = true)
    public List<FactCheckHistoryResponse> getHistory(
            Long postId
    ) {
        findPublishedPost(postId);

        Long selectedAnalysisId =
                selectionRepository
                        .findByPost_Id(postId)
                        .map(selection ->
                                selection
                                        .getAnalysis()
                                        .getId()
                        )
                        .orElse(null);

        return analysisRepository
                .findAllByPost_IdOrderByRunNumberDesc(
                        postId
                )
                .stream()
                .map(analysis -> {
                    boolean selected =
                            selectedAnalysisId != null
                                    && selectedAnalysisId.equals(
                                    analysis.getId()
                            );

                    return FactCheckHistoryResponse.from(
                            analysis,
                            selected
                    );
                })
                .toList();
    }

    /*
     * 게시글에 속한 특정 분석 상세 조회
     *
     * postId와 analysisId를 함께 검사하여
     * 다른 게시글의 분석이 노출되지 않도록 한다.
     */
    @Transactional(readOnly = true)
    public FactCheckResponse getDetail(
            Long postId,
            Long analysisId
    ) {
        findPublishedPost(postId);

        FactCheckAnalysis analysis =
                analysisRepository
                        .findByIdAndPost_Id(
                                analysisId,
                                postId
                        )
                        .orElseThrow(
                                FactCheckException::notFound
                        );

        return FactCheckResponse.from(
                analysis
        );
    }

    /*
     * 게시글의 대표 분석 변경
     *
     * 게시글 작성자 또는 관리자만 변경할 수 있다.
     * 해당 게시글에 속하면서 정상 완료된 분석만 선택한다.
     */
    @Transactional
    public FactCheckResponse changeRepresentative(
            Long postId,
            Long analysisId,
            String userEmail
    ) {
        Post post =
                findPublishedPost(postId);

        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        validateRequestPermission(
                post,
                user
        );

        FactCheckAnalysis analysis =
                analysisRepository
                        .findByIdAndPost_Id(
                                analysisId,
                                postId
                        )
                        .orElseThrow(
                                FactCheckException::notFound
                        );

        if (!analysis.isCompleted()) {
            throw FactCheckException
                    .invalidSelection();
        }

        selectionRepository
                .findByPost_Id(postId)
                .ifPresentOrElse(
                        selection ->
                                selection.changeAnalysis(
                                        analysis,
                                        user
                                ),

                        () -> {
                            PostAnalysisSelection selection =
                                    PostAnalysisSelection.create(
                                            post,
                                            analysis,
                                            user
                                    );

                            selectionRepository.save(
                                    selection
                            );
                        }
                );

        return FactCheckResponse.from(
                analysis
        );
    }

    /*
     * 최초 성공 분석은 자동으로 대표 분석이 된다.
     *
     * 기존 대표 분석이 stale 상태라면
     * 현재 게시글 내용을 분석한 새 결과로 교체한다.
     *
     * 일반 재분석에서는 기존 대표 분석을 유지한다.
     */
    private void updateRepresentativeAnalysis(
            FactCheckAnalysis newAnalysis
    ) {
        Long postId =
                newAnalysis
                        .getPost()
                        .getId();

        selectionRepository
                .findByPost_Id(postId)
                .ifPresentOrElse(
                        selection -> {

                            FactCheckAnalysis current =
                                    selection
                                            .getAnalysis();

                            if (current.isStale()) {
                                selection.changeAnalysis(
                                        newAnalysis,
                                        newAnalysis
                                                .getRequestedBy()
                                );
                            }
                        },

                        () -> {
                            PostAnalysisSelection selection =
                                    PostAnalysisSelection.create(
                                            newAnalysis
                                                    .getPost(),
                                            newAnalysis,
                                            newAnalysis
                                                    .getRequestedBy()
                                    );

                            selectionRepository.save(
                                    selection
                            );
                        }
                );
    }

    /*
     * 공개 게시글 조회
     */
    private Post findPublishedPost(
            Long postId
    ) {
        return postRepository
                .findByIdAndStatus(
                        postId,
                        PostStatus.PUBLISHED
                )
                .orElseThrow(
                        PostNotFoundException::new
                );
    }

    /*
     * 게시글 작성자 또는 관리자만
     * 팩트체크를 실행할 수 있다.
     */
    private void validateRequestPermission(
            Post post,
            User user
    ) {
        boolean isAuthor =
                post.isWrittenBy(
                        user.getId()
                );

        boolean isAdmin =
                user.getRole()
                        == UserRole.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new PostAccessDeniedException();
        }
    }

    /*
     * URL에서 도메인 추출
     */
    private String extractDomain(
            String url
    ) {
        try {
            return URI
                    .create(url)
                    .getHost();

        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /*
     * SHA-256 문자열 생성
     */
    private String createSha256(
            String value
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.",
                    exception
            );
        }
    }

    private String normalizeRequired(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            throw FactCheckException
                    .invalidResponse();
        }

        return value.trim();
    }

    private String normalizeOptional(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private String limit(
            String value,
            int maxLength
    ) {
        if (value == null) {
            return null;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(
                0,
                maxLength
        );
    }

    public record FactCheckJob(

            Long analysisId,
            Integer runNumber,
            String postTitle,
            String postContent

    ) {
    }
}