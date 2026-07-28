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
        name = "fact_check_sources",
        uniqueConstraints = {
                @UniqueConstraint(
                        name =
                                "uk_fact_check_sources_analysis_order",
                        columnNames = {
                                "analysis_id",
                                "source_order"
                        }
                ),
                @UniqueConstraint(
                        name =
                                "uk_fact_check_sources_analysis_url_hash",
                        columnNames = {
                                "analysis_id",
                                "url_hash"
                        }
                )
        }
)
public class FactCheckSource {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    /*
     * 이 출처가 사용된 팩트체크 분석
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
                            "fk_fact_check_sources_analysis"
            )
    )
    private FactCheckAnalysis analysis;

    /*
     * 분석 결과에서 출처가 표시되는 순서
     */
    @Column(
            name = "source_order",
            nullable = false
    )
    private Integer sourceOrder;

    /*
     * 출처 제목
     */
    @Column(
            name = "title",
            nullable = false,
            length = 500
    )
    private String title;

    /*
     * Gemini Grounding에서 반환된 실제 접근 URL
     *
     * vertexaisearch 리다이렉트 URL일 수도 있다.
     */
    @Column(
            name = "url",
            nullable = false,
            length = 2048
    )
    private String url;

    /*
     * 리다이렉트를 제거한 원본 URL
     *
     * 현재 알아낼 수 없는 경우 null을 허용한다.
     */
    @Column(
            name = "canonical_url",
            length = 2048
    )
    private String canonicalUrl;

    /*
     * URL의 SHA-256 해시값
     *
     * 긴 URL 자체를 UNIQUE 인덱스로 사용하지 않고
     * 64자리 해시값으로 중복 여부를 판단한다.
     */
    @Column(
            name = "url_hash",
            nullable = false,
            length = 64
    )
    private String urlHash;

    /*
     * 출처 도메인
     *
     * 예:
     * studyinkorea.go.kr
     * news.example.com
     */
    @Column(
            name = "domain",
            length = 255
    )
    private String domain;

    /*
     * 출처 유형
     *
     * 정부기관, 공공기관, 언론,
     * 학술 자료, 블로그 등을 구분한다.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "source_type",
            nullable = false,
            length = 30
    )
    private FactCheckSourceType sourceType;

    /*
     * 출처 전체에 대한 간단한 내용 요약
     *
     * 주장별 직접 근거 문장은
     * FactCheckEvidence.snippet에 따로 저장한다.
     */
    @Column(
            name = "snippet",
            length = 2000
    )
    private String snippet;

    /*
     * 해당 자료가 실제로 게시된 시각
     *
     * Gemini가 게시 시각을 제공하지 않으면 null이다.
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /*
     * 서버가 해당 출처를 검색하거나 받아온 시각
     */
    @Column(
            name = "retrieved_at",
            nullable = false
    )
    private LocalDateTime retrievedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    protected FactCheckSource() {
    }

    private FactCheckSource(
            FactCheckAnalysis analysis,
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
        this.analysis = analysis;
        this.sourceOrder = sourceOrder;
        this.title = title;
        this.url = url;
        this.canonicalUrl = canonicalUrl;
        this.urlHash = urlHash;
        this.domain = domain;
        this.sourceType = sourceType;
        this.snippet = snippet;
        this.publishedAt = publishedAt;

        this.retrievedAt =
                retrievedAt != null
                        ? retrievedAt
                        : LocalDateTime.now();
    }

    /*
     * 새로운 분석 출처 생성
     */
    public static FactCheckSource create(
            FactCheckAnalysis analysis,
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
        validateRequiredValues(
                analysis,
                title,
                url,
                urlHash,
                sourceType
        );

        validateSourceOrder(sourceOrder);
        validateUrlHash(urlHash);

        return new FactCheckSource(
                analysis,
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
    }

    /*
     * 필수 값 검증
     */
    private static void validateRequiredValues(
            FactCheckAnalysis analysis,
            String title,
            String url,
            String urlHash,
            FactCheckSourceType sourceType
    ) {
        if (analysis == null) {
            throw new IllegalArgumentException(
                    "출처가 속할 분석은 필수입니다."
            );
        }

        if (title == null
                || title.isBlank()) {

            throw new IllegalArgumentException(
                    "출처 제목은 필수입니다."
            );
        }

        if (url == null
                || url.isBlank()) {

            throw new IllegalArgumentException(
                    "출처 URL은 필수입니다."
            );
        }

        if (urlHash == null
                || urlHash.isBlank()) {

            throw new IllegalArgumentException(
                    "출처 URL 해시값은 필수입니다."
            );
        }

        if (sourceType == null) {
            throw new IllegalArgumentException(
                    "출처 유형은 필수입니다."
            );
        }
    }

    /*
     * 출처 순서는 1 이상이어야 한다.
     */
    private static void validateSourceOrder(
            Integer sourceOrder
    ) {
        if (sourceOrder == null
                || sourceOrder < 1) {

            throw new IllegalArgumentException(
                    "출처 순서는 1 이상이어야 합니다."
            );
        }
    }

    /*
     * SHA-256 해시는 64자리 16진수여야 한다.
     */
    private static void validateUrlHash(
            String urlHash
    ) {
        if (!urlHash.matches(
                "^[0-9a-fA-F]{64}$"
        )) {
            throw new IllegalArgumentException(
                    "출처 URL 해시값은 64자리 SHA-256 형식이어야 합니다."
            );
        }
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now =
                LocalDateTime.now();

        this.createdAt = now;

        if (this.retrievedAt == null) {
            this.retrievedAt = now;
        }
    }

    public Long getId() {
        return id;
    }

    public FactCheckAnalysis getAnalysis() {
        return analysis;
    }

    public Integer getSourceOrder() {
        return sourceOrder;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public String getUrlHash() {
        return urlHash;
    }

    public String getDomain() {
        return domain;
    }

    public FactCheckSourceType getSourceType() {
        return sourceType;
    }

    public String getSnippet() {
        return snippet;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public LocalDateTime getRetrievedAt() {
        return retrievedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}