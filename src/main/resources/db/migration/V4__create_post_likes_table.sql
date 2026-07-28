CREATE TABLE post_likes
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    post_id     BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_post_likes
        PRIMARY KEY (id),

    CONSTRAINT fk_post_likes_post
        FOREIGN KEY (post_id)
            REFERENCES posts (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_post_likes_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT uk_post_likes_post_user
        UNIQUE (post_id, user_id),

    INDEX idx_post_likes_post_id (
        post_id
    ),

    INDEX idx_post_likes_user_id (
        user_id
    )
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;