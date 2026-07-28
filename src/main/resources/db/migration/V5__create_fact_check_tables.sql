CREATE TABLE fact_check_analyses
(
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    post_id             BIGINT        NOT NULL,
    requested_by        BIGINT        NOT NULL,

    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    verdict             VARCHAR(30)   NULL,
    credibility_score   INT           NULL,
    confidence_score    INT           NULL,

    summary             VARCHAR(1000) NULL,
    explanation         TEXT          NULL,

    model               VARCHAR(100)  NULL,
    interaction_id      VARCHAR(255)  NULL,
    error_message       VARCHAR(1000) NULL,

    created_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    completed_at        DATETIME(6)   NULL,

    CONSTRAINT pk_fact_check_analyses
        PRIMARY KEY (id),

    CONSTRAINT uk_fact_check_analyses_post
        UNIQUE (post_id),

    CONSTRAINT fk_fact_check_analyses_post
        FOREIGN KEY (post_id)
            REFERENCES posts (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_fact_check_analyses_requested_by
        FOREIGN KEY (requested_by)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_fact_check_analyses_status
        CHECK (
            status IN (
                       'PENDING',
                       'PROCESSING',
                       'COMPLETED',
                       'FAILED'
                )
            ),

    CONSTRAINT chk_fact_check_analyses_verdict
        CHECK (
            verdict IS NULL
                OR verdict IN (
                               'TRUE',
                               'MOSTLY_TRUE',
                               'MIXED',
                               'MOSTLY_FALSE',
                               'FALSE',
                               'UNVERIFIABLE'
                )
            ),

    CONSTRAINT chk_fact_check_credibility_score
        CHECK (
            credibility_score IS NULL
                OR credibility_score BETWEEN 0 AND 100
            ),

    CONSTRAINT chk_fact_check_confidence_score
        CHECK (
            confidence_score IS NULL
                OR confidence_score BETWEEN 0 AND 100
            ),

    INDEX idx_fact_check_requested_by (
        requested_by
    ),

    INDEX idx_fact_check_status_created_at (
        status,
        created_at
    )
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE fact_check_sources
(
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    analysis_id     BIGINT        NOT NULL,
    title           VARCHAR(500)  NOT NULL,
    url             VARCHAR(2048) NOT NULL,
    snippet         VARCHAR(2000) NULL,
    created_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_fact_check_sources
        PRIMARY KEY (id),

    CONSTRAINT fk_fact_check_sources_analysis
        FOREIGN KEY (analysis_id)
            REFERENCES fact_check_analyses (id)
            ON DELETE CASCADE,

    INDEX idx_fact_check_sources_analysis (
        analysis_id
    )
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;