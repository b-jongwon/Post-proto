package com.facthub.factcheck.domain;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "fact_check_evidences",
        uniqueConstraints = {
                @UniqueConstraint(
                        name =
                                "uk_fact_check_evidences_claim_order",
                        columnNames = {
                                "claim_id",
                                "evidence_order"
                        }
                )
        }
)
public class FactCheckEvidence {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    /*
     * 이 근거가 연결된 핵심 주장
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "claim_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name =
                            "fk_fact_check_evidences_claim"
            )
    )
    private FactCheckClaim claim;

    /*
     * 이 근거의 출처
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "source_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name =
                            "fk_fact_check_evidences_source"
            )
    )
    private FactCheckSource source;

    /*
     * 해당 주장 내부에서 근거의 표시 순서
     */
    @Column(
            name = "evidence_order",
            nullable = false
    )
    private Integer evidenceOrder;

    /*
     * 해당 출처가 주장에 어떤 역할을 하는지 나타낸다.
     *
     * SUPPORTS: 주장을 뒷받침
     * REFUTES: 주장을 반박
     * CONTEXT: 배경이나 맥락 제공
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "stance",
            nullable = false,
            length = 20
    )
    private EvidenceStance stance;

    /*
     * 출처에서 해당 주장을 검증하는 데
     * 직접 사용된 근거 문장 또는 요약
     */
    @Column(
            name = "snippet",
            columnDefinition = "TEXT"
    )
    private String snippet;

    /*
     * 이 출처가 왜 해당 주장을
     * 지지하거나 반박하는지에 대한 설명
     */
    @Column(
            name = "reasoning",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String reasoning;

    /*
     * 해당 출처와 주장의 관련성 점수
     *
     * 0 이상 100 이하
     */
    @Column(
            name = "relevance_score",
            nullable = false
    )
    private Integer relevanceScore;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    protected FactCheckEvidence() {
    }

    private FactCheckEvidence(
            FactCheckClaim claim,
            FactCheckSource source,
            Integer evidenceOrder,
            EvidenceStance stance,
            String snippet,
            String reasoning,
            Integer relevanceScore
    ) {
        this.claim = claim;
        this.source = source;
        this.evidenceOrder =
                evidenceOrder;
        this.stance = stance;
        this.snippet = snippet;
        this.reasoning = reasoning;
        this.relevanceScore =
                relevanceScore;
    }

    /*
     * 새로운 주장 근거 생성
     */
    public static FactCheckEvidence create(
            FactCheckClaim claim,
            FactCheckSource source,
            Integer evidenceOrder,
            EvidenceStance stance,
            String snippet,
            String reasoning,
            Integer relevanceScore
    ) {
        validateRequiredValues(
                claim,
                source,
                stance,
                reasoning
        );

        validateOrder(evidenceOrder);

        validateRelevanceScore(
                relevanceScore
        );

        validateSameAnalysis(
                claim,
                source
        );

        return new FactCheckEvidence(
                claim,
                source,
                evidenceOrder,
                stance,
                snippet,
                reasoning,
                relevanceScore
        );
    }

    /*
     * 필수 값 검증
     */
    private static void validateRequiredValues(
            FactCheckClaim claim,
            FactCheckSource source,
            EvidenceStance stance,
            String reasoning
    ) {
        if (claim == null) {
            throw new IllegalArgumentException(
                    "근거가 연결될 주장은 필수입니다."
            );
        }

        if (source == null) {
            throw new IllegalArgumentException(
                    "근거 출처는 필수입니다."
            );
        }

        if (stance == null) {
            throw new IllegalArgumentException(
                    "근거의 역할은 필수입니다."
            );
        }

        if (reasoning == null
                || reasoning.isBlank()) {

            throw new IllegalArgumentException(
                    "근거 설명은 필수입니다."
            );
        }
    }

    /*
     * 근거 순서는 1 이상이어야 한다.
     */
    private static void validateOrder(
            Integer evidenceOrder
    ) {
        if (evidenceOrder == null
                || evidenceOrder < 1) {

            throw new IllegalArgumentException(
                    "근거 순서는 1 이상이어야 합니다."
            );
        }
    }

    /*
     * 관련성 점수는 0 이상 100 이하여야 한다.
     */
    private static void validateRelevanceScore(
            Integer relevanceScore
    ) {
        if (relevanceScore == null
                || relevanceScore < 0
                || relevanceScore > 100) {

            throw new IllegalArgumentException(
                    "근거 관련성 점수는 0 이상 100 이하여야 합니다."
            );
        }
    }

    /*
     * Claim과 Source는 반드시
     * 동일한 Analysis에 속해야 한다.
     *
     * 다른 분석의 출처를 잘못 연결하는 것을 방지한다.
     */
    private static void validateSameAnalysis(
            FactCheckClaim claim,
            FactCheckSource source
    ) {
        FactCheckAnalysis claimAnalysis =
                claim.getAnalysis();

        FactCheckAnalysis sourceAnalysis =
                source.getAnalysis();

        if (claimAnalysis == sourceAnalysis) {
            return;
        }

        Long claimAnalysisId =
                claimAnalysis.getId();

        Long sourceAnalysisId =
                sourceAnalysis.getId();

        boolean samePersistedAnalysis =
                claimAnalysisId != null
                        && sourceAnalysisId != null
                        && claimAnalysisId.equals(
                        sourceAnalysisId
                );

        if (!samePersistedAnalysis) {
            throw new IllegalArgumentException(
                    "주장과 출처는 같은 분석에 속해야 합니다."
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

    public FactCheckClaim getClaim() {
        return claim;
    }

    public FactCheckSource getSource() {
        return source;
    }

    public Integer getEvidenceOrder() {
        return evidenceOrder;
    }

    public EvidenceStance getStance() {
        return stance;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getReasoning() {
        return reasoning;
    }

    public Integer getRelevanceScore() {
        return relevanceScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}