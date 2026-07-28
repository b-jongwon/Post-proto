-- FactHub 팩트체크 이력 + 주장(claim) + 근거(evidence) 구조 확장
-- 기존 V5 파일은 수정하지 않고 V6로 추가한다.

/* =========================================================
   1. fact_check_analyses
   - 게시글당 1개 제한 제거
   - 실행 순번, 프롬프트/스키마 버전, 게시글 스냅샷 추가
   ========================================================= */

ALTER TABLE fact_check_analyses
    ADD COLUMN run_number INT NULL AFTER post_id,
    ADD COLUMN prompt_version VARCHAR(50) NULL AFTER interaction_id,
    ADD COLUMN schema_version VARCHAR(50) NULL AFTER prompt_version,
    ADD COLUMN post_title_snapshot VARCHAR(200) NULL AFTER schema_version,
    ADD COLUMN post_content_snapshot LONGTEXT NULL AFTER post_title_snapshot,
    ADD COLUMN post_content_hash CHAR(64) NULL AFTER post_content_snapshot,
    ADD COLUMN is_stale BOOLEAN NOT NULL DEFAULT FALSE AFTER post_content_hash;

UPDATE fact_check_analyses analysis
    INNER JOIN posts post
ON post.id = analysis.post_id
    SET analysis.run_number = 1,
        analysis.prompt_version = 'legacy-v1',
        analysis.schema_version = 'legacy-v1',
        analysis.post_title_snapshot = post.title,
        analysis.post_content_snapshot = post.content,
        analysis.post_content_hash = LOWER(
        SHA2(
        CONCAT(post.title, '\n', post.content),
        256
        )
        );

-- 기존 post_id FK가 사용할 수 있도록 복합 UNIQUE를 먼저 만든 뒤
-- 기존 게시글당 1개 UNIQUE 인덱스를 제거한다.
ALTER TABLE fact_check_analyses
    ADD CONSTRAINT uk_fact_check_analyses_post_run
        UNIQUE (post_id, run_number);

ALTER TABLE fact_check_analyses
DROP INDEX uk_fact_check_analyses_post;

ALTER TABLE fact_check_analyses
    MODIFY COLUMN run_number INT NOT NULL,
    MODIFY COLUMN prompt_version VARCHAR(50) NOT NULL,
    MODIFY COLUMN schema_version VARCHAR(50) NOT NULL,
    MODIFY COLUMN post_title_snapshot VARCHAR(200) NOT NULL,
    MODIFY COLUMN post_content_snapshot LONGTEXT NOT NULL,
    MODIFY COLUMN post_content_hash CHAR(64) NOT NULL,
    ADD CONSTRAINT chk_fact_check_analyses_run_number
    CHECK (run_number >= 1),
    ADD INDEX idx_fact_check_analyses_post_created_at (
    post_id,
    created_at
    ),
    ADD INDEX idx_fact_check_analyses_post_stale (
    post_id,
    is_stale
    );


/* =========================================================
   2. fact_check_sources 확장
   ========================================================= */

ALTER TABLE fact_check_sources
    ADD COLUMN source_order INT NULL AFTER analysis_id,
    ADD COLUMN canonical_url VARCHAR(2048) NULL AFTER url,
    ADD COLUMN url_hash CHAR(64) NULL AFTER canonical_url,
    ADD COLUMN domain VARCHAR(255) NULL AFTER url_hash,
    ADD COLUMN source_type VARCHAR(30) NULL AFTER domain,
    ADD COLUMN published_at DATETIME(6) NULL AFTER snippet,
    ADD COLUMN retrieved_at DATETIME(6) NULL AFTER published_at;

-- 동일 분석 안에 완전히 같은 URL이 중복 저장된 기존 데이터가 있다면
-- 가장 먼저 저장된 행 하나만 남긴다.
DELETE duplicate_source
FROM fact_check_sources duplicate_source
    INNER JOIN fact_check_sources original_source
        ON original_source.analysis_id = duplicate_source.analysis_id
        AND original_source.url = duplicate_source.url
        AND original_source.id < duplicate_source.id;

UPDATE fact_check_sources
SET canonical_url = url,
    url_hash = LOWER(SHA2(url, 256)),
    source_type = 'OTHER',
    retrieved_at = created_at;

CREATE TEMPORARY TABLE tmp_fact_check_source_order AS
SELECT id,
       ROW_NUMBER() OVER (
           PARTITION BY analysis_id
           ORDER BY id
       ) AS calculated_order
FROM fact_check_sources;

UPDATE fact_check_sources source
    INNER JOIN tmp_fact_check_source_order source_order
ON source_order.id = source.id
    SET source.source_order = source_order.calculated_order;

DROP TEMPORARY TABLE tmp_fact_check_source_order;

ALTER TABLE fact_check_sources
    MODIFY COLUMN source_order INT NOT NULL,
    MODIFY COLUMN url_hash CHAR(64) NOT NULL,
    MODIFY COLUMN source_type VARCHAR(30) NOT NULL,
    MODIFY COLUMN retrieved_at DATETIME(6) NOT NULL,
    ADD CONSTRAINT uk_fact_check_sources_analysis_order
    UNIQUE (analysis_id, source_order),
    ADD CONSTRAINT uk_fact_check_sources_analysis_url_hash
    UNIQUE (analysis_id, url_hash),
    ADD CONSTRAINT chk_fact_check_sources_order
    CHECK (source_order >= 1),
    ADD CONSTRAINT chk_fact_check_sources_type
    CHECK (
    source_type IN (
    'GOVERNMENT',
    'PUBLIC_INSTITUTION',
    'ACADEMIC',
    'PRIMARY_SOURCE',
    'NEWS',
    'ENCYCLOPEDIA',
    'COMMUNITY',
    'BLOG',
    'VIDEO',
    'OTHER'
    )
    );


/* =========================================================
   3. 분석별 핵심 주장
   ========================================================= */

CREATE TABLE fact_check_claims
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    analysis_id         BIGINT       NOT NULL,
    claim_order         INT          NOT NULL,

    claim_text          TEXT         NOT NULL,
    normalized_claim    TEXT         NOT NULL,

    verdict             VARCHAR(30)  NOT NULL,
    confidence_score    INT          NOT NULL,
    explanation         TEXT         NOT NULL,

    created_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_fact_check_claims
        PRIMARY KEY (id),

    CONSTRAINT uk_fact_check_claims_analysis_order
        UNIQUE (analysis_id, claim_order),

    CONSTRAINT fk_fact_check_claims_analysis
        FOREIGN KEY (analysis_id)
            REFERENCES fact_check_analyses (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_fact_check_claims_order
        CHECK (claim_order >= 1),

    CONSTRAINT chk_fact_check_claims_verdict
        CHECK (
            verdict IN (
                        'TRUE',
                        'MOSTLY_TRUE',
                        'MIXED',
                        'MOSTLY_FALSE',
                        'FALSE',
                        'UNVERIFIABLE'
                )
            ),

    CONSTRAINT chk_fact_check_claims_confidence_score
        CHECK (confidence_score BETWEEN 0 AND 100),

    INDEX idx_fact_check_claims_analysis (
        analysis_id
    )
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


/* =========================================================
   4. 주장과 출처를 연결하는 근거
   ========================================================= */

CREATE TABLE fact_check_evidences
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    claim_id            BIGINT       NOT NULL,
    source_id           BIGINT       NOT NULL,
    evidence_order      INT          NOT NULL,

    stance              VARCHAR(20)  NOT NULL,
    snippet             TEXT         NULL,
    reasoning           TEXT         NOT NULL,
    relevance_score     INT          NOT NULL,

    created_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_fact_check_evidences
        PRIMARY KEY (id),

    CONSTRAINT uk_fact_check_evidences_claim_order
        UNIQUE (claim_id, evidence_order),

    CONSTRAINT fk_fact_check_evidences_claim
        FOREIGN KEY (claim_id)
            REFERENCES fact_check_claims (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_fact_check_evidences_source
        FOREIGN KEY (source_id)
            REFERENCES fact_check_sources (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_fact_check_evidences_order
        CHECK (evidence_order >= 1),

    CONSTRAINT chk_fact_check_evidences_stance
        CHECK (
            stance IN (
                       'SUPPORTS',
                       'REFUTES',
                       'CONTEXT'
                )
            ),

    CONSTRAINT chk_fact_check_evidences_relevance_score
        CHECK (relevance_score BETWEEN 0 AND 100),

    INDEX idx_fact_check_evidences_claim (
        claim_id
    ),

    INDEX idx_fact_check_evidences_source (
        source_id
    )
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


/* =========================================================
   5. 게시글별 대표 분석 선택
   ========================================================= */

CREATE TABLE post_analysis_selections
(
    post_id             BIGINT       NOT NULL,
    analysis_id         BIGINT       NOT NULL,
    selected_by         BIGINT       NOT NULL,
    selected_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_post_analysis_selections
        PRIMARY KEY (post_id),

    CONSTRAINT uk_post_analysis_selections_analysis
        UNIQUE (analysis_id),

    CONSTRAINT fk_post_analysis_selections_post
        FOREIGN KEY (post_id)
            REFERENCES posts (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_post_analysis_selections_analysis
        FOREIGN KEY (analysis_id)
            REFERENCES fact_check_analyses (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_post_analysis_selections_user
        FOREIGN KEY (selected_by)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    INDEX idx_post_analysis_selections_selected_by (
        selected_by
    )
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

-- V5 구조에서는 게시글당 분석이 최대 1개였으므로,
-- 기존에 완료된 분석은 해당 게시글의 대표 분석으로 안전하게 이관한다.
INSERT INTO post_analysis_selections (
    post_id,
    analysis_id,
    selected_by,
    selected_at
)
SELECT analysis.post_id,
       analysis.id,
       analysis.requested_by,
       COALESCE(
               analysis.completed_at,
               analysis.updated_at,
               analysis.created_at
       )
FROM fact_check_analyses analysis
WHERE analysis.status = 'COMPLETED';