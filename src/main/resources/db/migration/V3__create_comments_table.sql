CREATE TABLE comments
(
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    post_id     BIGINT        NOT NULL,
    author_id   BIGINT        NOT NULL,
    content     VARCHAR(1000) NOT NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_comments
        PRIMARY KEY (id),

    CONSTRAINT fk_comments_post
        FOREIGN KEY (post_id)
            REFERENCES posts (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_comments_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'DELETED'
                )
            ),

    INDEX idx_comments_post_status_created_at (
        post_id,
        status,
        created_at
    ),

    INDEX idx_comments_author_id (
        author_id
    )
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;