/* =========================================================
   1. 회원 프로필과 이메일 인증 상태
   ========================================================= */

ALTER TABLE users
    ADD COLUMN full_name VARCHAR(50) NULL AFTER nickname,
    ADD COLUMN birth_year INT NULL AFTER full_name,
    ADD COLUMN email_verified_at DATETIME(6) NULL AFTER birth_year;

-- 기존 회원은 현재 서비스에서 이미 가입을 완료한 계정이므로 인증 완료로 이관한다.
UPDATE users
SET email_verified_at = created_at
WHERE email_verified_at IS NULL;

ALTER TABLE users
    MODIFY COLUMN email_verified_at DATETIME(6) NOT NULL,
    ADD CONSTRAINT chk_users_birth_year
        CHECK (
            birth_year IS NULL
                OR birth_year BETWEEN 1900 AND 2100
        );


/* =========================================================
   2. 일회용 이메일 인증 코드
   ========================================================= */

CREATE TABLE email_verifications
(
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    email           VARCHAR(255)  NOT NULL,
    code_hash       CHAR(64)      NOT NULL,
    token_hash      CHAR(64)      NULL,
    failed_attempts INT           NOT NULL DEFAULT 0,
    expires_at      DATETIME(6)   NOT NULL,
    verified_at     DATETIME(6)   NULL,
    consumed_at     DATETIME(6)   NULL,
    created_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_email_verifications
        PRIMARY KEY (id),

    CONSTRAINT uk_email_verifications_token_hash
        UNIQUE (token_hash),

    CONSTRAINT chk_email_verifications_failed_attempts
        CHECK (failed_attempts >= 0),

    INDEX idx_email_verifications_email_created_at (
        email,
        created_at
    ),

    INDEX idx_email_verifications_expires_at (
        expires_at
    )
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


/* =========================================================
   3. 사용자 알림
   ========================================================= */

CREATE TABLE notifications
(
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    recipient_id BIGINT        NOT NULL,
    actor_id     BIGINT        NOT NULL,
    post_id      BIGINT        NOT NULL,
    type         VARCHAR(30)   NOT NULL,
    message      VARCHAR(300)  NOT NULL,
    is_read      BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    read_at      DATETIME(6)   NULL,

    CONSTRAINT pk_notifications
        PRIMARY KEY (id),

    CONSTRAINT fk_notifications_recipient
        FOREIGN KEY (recipient_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_notifications_actor
        FOREIGN KEY (actor_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_notifications_post
        FOREIGN KEY (post_id)
            REFERENCES posts (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_notifications_type
        CHECK (
            type IN (
                'COMMENT_CREATED',
                'POST_LIKED'
            )
        ),

    INDEX idx_notifications_recipient_read_created (
        recipient_id,
        is_read,
        created_at
    ),

    INDEX idx_notifications_post (
        post_id
    )
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


/* =========================================================
   4. 관리자 작업 감사 로그
   ========================================================= */

CREATE TABLE admin_actions
(
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    admin_id    BIGINT        NOT NULL,
    action_type VARCHAR(40)   NOT NULL,
    target_type VARCHAR(30)   NOT NULL,
    target_id   BIGINT        NOT NULL,
    description VARCHAR(500)  NOT NULL,
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_admin_actions
        PRIMARY KEY (id),

    CONSTRAINT fk_admin_actions_admin
        FOREIGN KEY (admin_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    INDEX idx_admin_actions_admin_created (
        admin_id,
        created_at
    ),

    INDEX idx_admin_actions_target (
        target_type,
        target_id
    )
)
    ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


/* =========================================================
   5. 활동·통계 조회 인덱스
   ========================================================= */

ALTER TABLE posts
    ADD INDEX idx_posts_author_status_created (
        author_id,
        status,
        created_at
    ),
    ADD INDEX idx_posts_status_view_count (
        status,
        view_count
    );

ALTER TABLE comments
    ADD INDEX idx_comments_author_status_created (
        author_id,
        status,
        created_at
    ),
    ADD INDEX idx_comments_status_created_at (
        status,
        created_at
    );

ALTER TABLE post_likes
    ADD INDEX idx_post_likes_user_created (
        user_id,
        created_at
    ),
    ADD INDEX idx_post_likes_created_at (
        created_at
    );

