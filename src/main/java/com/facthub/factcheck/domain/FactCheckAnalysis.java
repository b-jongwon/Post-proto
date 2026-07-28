package com.facthub.factcheck.domain;

import com.facthub.post.domain.Post;
import com.facthub.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "fact_check_analyses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name =
                                "uk_fact_check_analyses_post_run",
                        columnNames = {
                                "post_id",
                                "run_number"
                        }
                )
        }
)
public class FactCheckAnalysis {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    /*
     * 어떤 게시글에 대한 분석인지 나타낸다.
     *
     * 게시글 하나에는 여러 분석이 존재할 수 있다.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "post_id",
            nullable = false
    )
    private Post post;

    /*
     * 해당 게시글의 몇 번째 분석인지 나타낸다.
     *
     * 예:
     * 첫 번째 분석: 1
     * 두 번째 분석: 2
     */
    @Column(
            name = "run_number",
            nullable = false
    )
    private Integer runNumber;

    /*
     * 분석을 요청한 사용자
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "requested_by",
            nullable = false
    )
    private User requestedBy;

    /*
     * 분석 실행 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private FactCheckStatus status;

    /*
     * 게시글 전체에 대한 종합 판정
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "verdict",
            length = 30
    )
    private FactCheckVerdict verdict;

    /*
     * 게시글 내용의 신뢰도 점수
     *
     * 0: 신뢰하기 어려움
     * 100: 신뢰도가 높음
     */
    @Column(name = "credibility_score")
    private Integer credibilityScore;

    /*
     * AI가 자신의 판정에 가지는 확신 정도
     */
    @Column(name = "confidence_score")
    private Integer confidenceScore;

    /*
     * 전체 분석 요약
     */
    @Column(
            name = "summary",
            length = 1000
    )
    private String summary;

    /*
     * 전체 분석의 상세 설명
     */
    @Column(
            name = "explanation",
            columnDefinition = "TEXT"
    )
    private String explanation;

    /*
     * 분석에 사용된 Gemini 모델명
     */
    @Column(
            name = "model",
            length = 100
    )
    private String model;

    /*
     * Gemini API 요청 또는 응답을 식별하기 위한 값
     */
    @Column(
            name = "interaction_id",
            length = 255
    )
    private String interactionId;

    /*
     * 사용한 프롬프트 버전
     *
     * 프롬프트가 바뀌었을 때
     * 어떤 방식으로 분석했는지 추적하기 위해 저장한다.
     */
    @Column(
            name = "prompt_version",
            nullable = false,
            length = 50
    )
    private String promptVersion;

    /*
     * Gemini 응답 JSON 구조의 버전
     *
     * 예:
     * legacy-v1
     * claims-v1
     */
    @Column(
            name = "schema_version",
            nullable = false,
            length = 50
    )
    private String schemaVersion;

    /*
     * 분석 당시 게시글 제목
     */
    @Column(
            name = "post_title_snapshot",
            nullable = false,
            length = 200
    )
    private String postTitleSnapshot;

    /*
     * 분석 당시 게시글 본문
     */
    @Column(
            name = "post_content_snapshot",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String postContentSnapshot;

    /*
     * 분석 당시 제목과 본문의 SHA-256 해시
     */
    @Column(
            name = "post_content_hash",
            nullable = false,
            length = 64
    )
    private String postContentHash;

    /*
     * 분석 이후 게시글이 수정되었는지 나타낸다.
     */
    @Column(
            name = "is_stale",
            nullable = false
    )
    private boolean stale;

    /*
     * 분석 실패 메시지
     */
    @Column(
            name = "error_message",
            length = 1000
    )
    private String errorMessage;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /*
     * 분석에서 추출된 핵심 주장 목록
     */
    @OneToMany(
            mappedBy = "analysis",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("claimOrder ASC")
    private List<FactCheckClaim> claims =
            new ArrayList<>();

    /*
     * 분석에 사용된 전체 출처 목록
     */
    @OneToMany(
            mappedBy = "analysis",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sourceOrder ASC")
    private List<FactCheckSource> sources =
            new ArrayList<>();

    protected FactCheckAnalysis() {
    }

    private FactCheckAnalysis(
            Post post,
            Integer runNumber,
            User requestedBy,
            String model,
            String promptVersion,
            String schemaVersion,
            String postTitleSnapshot,
            String postContentSnapshot,
            String postContentHash
    ) {
        this.post = post;
        this.runNumber = runNumber;
        this.requestedBy = requestedBy;
        this.model = model;
        this.promptVersion = promptVersion;
        this.schemaVersion = schemaVersion;
        this.postTitleSnapshot =
                postTitleSnapshot;
        this.postContentSnapshot =
                postContentSnapshot;
        this.postContentHash =
                postContentHash;

        this.status =
                FactCheckStatus.PROCESSING;

        this.stale = false;
    }

    /*
     * 새로운 분석 실행 객체 생성
     *
     * 재분석하더라도 기존 객체를 재사용하지 않고
     * 항상 새로운 FactCheckAnalysis를 만든다.
     */
    public static FactCheckAnalysis start(
            Post post,
            Integer runNumber,
            User requestedBy,
            String model,
            String promptVersion,
            String schemaVersion,
            String postTitleSnapshot,
            String postContentSnapshot,
            String postContentHash
    ) {
        return new FactCheckAnalysis(
                post,
                runNumber,
                requestedBy,
                model,
                promptVersion,
                schemaVersion,
                postTitleSnapshot,
                postContentSnapshot,
                postContentHash
        );
    }

    /*
     * Gemini 분석 성공 처리
     */
    public void complete(
            FactCheckVerdict verdict,
            Integer credibilityScore,
            Integer confidenceScore,
            String summary,
            String explanation,
            String interactionId,
            String model
    ) {
        this.status =
                FactCheckStatus.COMPLETED;

        this.verdict = verdict;
        this.credibilityScore =
                credibilityScore;
        this.confidenceScore =
                confidenceScore;
        this.summary = summary;
        this.explanation = explanation;
        this.interactionId =
                interactionId;
        this.model = model;

        this.errorMessage = null;
        this.completedAt =
                LocalDateTime.now();
    }

    /*
     * Gemini 분석 실패 처리
     */
    public void fail(String errorMessage) {
        this.status =
                FactCheckStatus.FAILED;

        this.errorMessage =
                errorMessage;

        this.completedAt =
                LocalDateTime.now();
    }

    /*
     * 게시글이 수정되었을 때
     * 기존 분석을 오래된 분석으로 표시한다.
     */
    public void markStale() {
        this.stale = true;
    }

    /*
     * 분석에 핵심 주장을 추가한다.
     */
    public FactCheckClaim addClaim(
            Integer claimOrder,
            String claimText,
            String normalizedClaim,
            FactCheckVerdict verdict,
            Integer confidenceScore,
            String explanation
    ) {
        FactCheckClaim claim =
                FactCheckClaim.create(
                        this,
                        claimOrder,
                        claimText,
                        normalizedClaim,
                        verdict,
                        confidenceScore,
                        explanation
                );

        this.claims.add(claim);

        return claim;
    }

    /*
     * 분석에 출처를 추가한다.
     */
    public FactCheckSource addSource(
            Integer sourceOrder,
            String title,
            String url,
            String canonicalUrl,
            String urlHash,
            String domain,
            FactCheckSourceType sourceType,
            String snippet,
            LocalDateTime publishedAt,
            LocalDateTime retrievedAt
    ) {
        FactCheckSource source =
                FactCheckSource.create(
                        this,
                        sourceOrder,
                        title,
                        url,
                        canonicalUrl,
                        urlHash,
                        domain,
                        sourceType,
                        snippet,
                        publishedAt,
                        retrievedAt
                );

        this.sources.add(source);

        return source;
    }

    /*
     * 해당 분석이 정상적으로 완료되었는지 확인한다.
     */
    public boolean isCompleted() {
        return this.status
                == FactCheckStatus.COMPLETED;
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now =
                LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt =
                LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Integer getRunNumber() {
        return runNumber;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public FactCheckStatus getStatus() {
        return status;
    }

    public FactCheckVerdict getVerdict() {
        return verdict;
    }

    public Integer getCredibilityScore() {
        return credibilityScore;
    }

    public Integer getConfidenceScore() {
        return confidenceScore;
    }

    public String getSummary() {
        return summary;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getModel() {
        return model;
    }

    public String getInteractionId() {
        return interactionId;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getPostTitleSnapshot() {
        return postTitleSnapshot;
    }

    public String getPostContentSnapshot() {
        return postContentSnapshot;
    }

    public String getPostContentHash() {
        return postContentHash;
    }

    public boolean isStale() {
        return stale;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public List<FactCheckClaim> getClaims() {
        return claims;
    }

    public List<FactCheckSource> getSources() {
        return sources;
    }
}