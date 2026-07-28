CREATE TABLE posts
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    author_id   BIGINT       NOT NULL,
    title       VARCHAR(200) NOT NULL,
    content     LONGTEXT     NOT NULL,
    category    VARCHAR(50)  NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED',
    view_count  BIGINT       NOT NULL DEFAULT 0,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_posts
        PRIMARY KEY (id),

    CONSTRAINT fk_posts_author
        FOREIGN KEY (author_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_posts_status
        CHECK (
            status IN (
                       'PUBLISHED',
                       'HIDDEN',
                       'DELETED'
                )
            ),

    INDEX idx_posts_author_id (author_id),
    INDEX idx_posts_status_created_at (
        status,
        created_at
    )
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;