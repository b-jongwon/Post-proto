package com.facthub.factcheck.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "fact_check_claims",
        uniqueConstraints = {
                @UniqueConstraint(
                        name =
                                "uk_fact_check_claims_analysis_order",
                        columnNames = {
                                "analysis_id",
                                "claim_order"
                        }
                )
        }
)
public class FactCheckClaim {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    /*
     * 이 주장이 포함된 전체 팩트체크 분석
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "analysis_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name =
                            "fk_fact_check_claims_analysis"
            )
    )
    private FactCheckAnalysis analysis;

    /*
     * 분석 결과 안에서 주장의 표시 순서
     *
     * 예:
     * 첫 번째 주장: 1
     * 두 번째 주장: 2
     */
    @Column(
            name = "claim_order",
            nullable = false
    )
    private Integer claimOrder;

    /*
     * 게시글 원문에서 추출한 주장
     *
     * 예:
     * "대한민국의 수도는 부산이다."
     */
    @Column(
            name = "claim_text",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String claimText;

    /*
     * 검증하기 쉽도록 명확하게 정리한 주장
     *
     * 예:
     * "대한민국의 공식 수도는 부산광역시이다."
     */
    @Column(
            name = "normalized_claim",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String normalizedClaim;

    /*
     * 해당 주장에 대한 개별 판정
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "verdict",
            nullable = false,
            length = 30
    )
    private FactCheckVerdict verdict;

    /*
     * 해당 주장 판정에 대한 AI 확신도
     *
     * 0 이상 100 이하
     */
    @Column(
            name = "confidence_score",
            nullable = false
    )
    private Integer confidenceScore;

    /*
     * 해당 주장에 대한 상세 설명
     */
    @Column(
            name = "explanation",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String explanation;

    /*
     * 해당 주장에 연결된 근거 목록
     */
    @OneToMany(
            mappedBy = "claim",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("evidenceOrder ASC")
    private List<FactCheckEvidence> evidences =
            new ArrayList<>();

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    protected FactCheckClaim() {
    }

    private FactCheckClaim(
            FactCheckAnalysis analysis,
            Integer claimOrder,
            String claimText,
            String normalizedClaim,
            FactCheckVerdict verdict,
            Integer confidenceScore,
            String explanation
    ) {
        this.analysis = analysis;
        this.claimOrder = claimOrder;
        this.claimText = claimText;
        this.normalizedClaim =
                normalizedClaim;
        this.verdict = verdict;
        this.confidenceScore =
                confidenceScore;
        this.explanation = explanation;
    }

    /*
     * 새로운 핵심 주장 생성
     */
    public static FactCheckClaim create(
            FactCheckAnalysis analysis,
            Integer claimOrder,
            String claimText,
            String normalizedClaim,
            FactCheckVerdict verdict,
            Integer confidenceScore,
            String explanation
    ) {
        validateOrder(claimOrder);
        validateConfidenceScore(
                confidenceScore
        );

        return new FactCheckClaim(
                analysis,
                claimOrder,
                claimText,
                normalizedClaim,
                verdict,
                confidenceScore,
                explanation
        );
    }

    /*
     * 해당 주장에 근거를 추가한다.
     *
     * Evidence는 Claim과 Source를 연결한다.
     */
    public FactCheckEvidence addEvidence(
            FactCheckSource source,
            Integer evidenceOrder,
            EvidenceStance stance,
            String snippet,
            String reasoning,
            Integer relevanceScore
    ) {
        FactCheckEvidence evidence =
                FactCheckEvidence.create(
                        this,
                        source,
                        evidenceOrder,
                        stance,
                        snippet,
                        reasoning,
                        relevanceScore
                );

        this.evidences.add(evidence);

        return evidence;
    }

    /*
     * 주장 순서는 1 이상이어야 한다.
     */
    private static void validateOrder(
            Integer claimOrder
    ) {
        if (claimOrder == null
                || claimOrder < 1) {

            throw new IllegalArgumentException(
                    "주장 순서는 1 이상이어야 합니다."
            );
        }
    }

    /*
     * AI 확신도는 0 이상 100 이하여야 한다.
     */
    private static void validateConfidenceScore(
            Integer confidenceScore
    ) {
        if (confidenceScore == null
                || confidenceScore < 0
                || confidenceScore > 100) {

            throw new IllegalArgumentException(
                    "주장 확신도는 0 이상 100 이하여야 합니다."
            );
        }
    }

    @PrePersist
    private void prePersist() {
        this.createdAt =
                LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public FactCheckAnalysis getAnalysis() {
        return analysis;
    }

    public Integer getClaimOrder() {
        return claimOrder;
    }

    public String getClaimText() {
        return claimText;
    }

    public String getNormalizedClaim() {
        return normalizedClaim;
    }

    public FactCheckVerdict getVerdict() {
        return verdict;
    }

    public Integer getConfidenceScore() {
        return confidenceScore;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<FactCheckEvidence> getEvidences() {
        return evidences;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}