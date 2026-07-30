package com.facthub.post.repository;

import com.facthub.post.domain.Post;
import com.facthub.post.domain.PostStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository
        extends JpaRepository<Post, Long> {

    /*
     * 게시글 목록 검색
     *
     * 검색 대상:
     * - 제목
     * - 본문
     * - 작성자 닉네임
     *
     * 필터:
     * - 게시글 상태
     * - 카테고리
     */
    @EntityGraph(attributePaths = "author")
    @Query(
            value = """
                    SELECT post
                    FROM Post post
                    WHERE post.status = :status
                      AND (
                            :keyword IS NULL
                            OR LOWER(post.title)
                                LIKE LOWER(
                                    CONCAT('%', :keyword, '%')
                                )
                            OR LOWER(post.content)
                                LIKE LOWER(
                                    CONCAT('%', :keyword, '%')
                                )
                            OR LOWER(post.author.nickname)
                                LIKE LOWER(
                                    CONCAT('%', :keyword, '%')
                                )
                      )
                      AND (
                            :category IS NULL
                            OR LOWER(post.category)
                                = LOWER(:category)
                      )
                    """,
            countQuery = """
                    SELECT COUNT(post)
                    FROM Post post
                    WHERE post.status = :status
                      AND (
                            :keyword IS NULL
                            OR LOWER(post.title)
                                LIKE LOWER(
                                    CONCAT('%', :keyword, '%')
                                )
                            OR LOWER(post.content)
                                LIKE LOWER(
                                    CONCAT('%', :keyword, '%')
                                )
                            OR LOWER(post.author.nickname)
                                LIKE LOWER(
                                    CONCAT('%', :keyword, '%')
                                )
                      )
                      AND (
                            :category IS NULL
                            OR LOWER(post.category)
                                = LOWER(:category)
                      )
                    """
    )
    Page<Post> searchPosts(
            @Param("status")
            PostStatus status,

            @Param("keyword")
            String keyword,

            @Param("category")
            String category,

            Pageable pageable
    );

    /*
     * 공개 게시글 개수 조회
     *
     * 홈 통계에서 전체 공개 게시글 수를 계산할 때 사용한다.
     */
    long countByStatus(
            PostStatus status
    );

    long countByAuthor_IdAndStatusNot(
            Long authorId,
            PostStatus excludedStatus
    );

    long countByStatusAndCreatedAtBetween(
            PostStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    @EntityGraph(attributePaths = "author")
    @Query("""
            SELECT post
            FROM Post post
            WHERE post.author.id = :authorId
              AND post.status <> :excludedStatus
            ORDER BY post.createdAt DESC,
                     post.id DESC
            """)
    List<Post> findRecentByAuthor(
            @Param("authorId")
            Long authorId,
            @Param("excludedStatus")
            PostStatus excludedStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "author")
    @Query("""
            SELECT post
            FROM Post post
            WHERE post.status <> :excludedStatus
            ORDER BY post.createdAt DESC,
                     post.id DESC
            """)
    Page<Post> findAdminPosts(
            @Param("excludedStatus")
            PostStatus excludedStatus,
            Pageable pageable
    );


    @EntityGraph(attributePaths = "author")
    @Query(
            value = """
                    SELECT post
                    FROM Post post
                    LEFT JOIN PostLike postLike
                      ON postLike.post = post
                    WHERE post.status = :status
                      AND (
                            :keyword IS NULL
                            OR LOWER(post.title)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(post.content)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(post.author.nickname)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                      AND (
                            :category IS NULL
                            OR LOWER(post.category)
                                = LOWER(:category)
                      )
                    GROUP BY post
                    ORDER BY COUNT(postLike.id) DESC,
                             post.createdAt DESC,
                             post.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(post)
                    FROM Post post
                    WHERE post.status = :status
                      AND (
                            :keyword IS NULL
                            OR LOWER(post.title)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(post.content)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(post.author.nickname)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                      AND (
                            :category IS NULL
                            OR LOWER(post.category)
                                = LOWER(:category)
                      )
                    """
    )
    Page<Post> searchPostsByLikes(
            @Param("status")
            PostStatus status,
            @Param("keyword")
            String keyword,
            @Param("category")
            String category,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "author")
    @Query(
            value = """
                    SELECT post
                    FROM Post post
                    LEFT JOIN PostLike postLike
                      ON postLike.post = post
                    LEFT JOIN Comment comment
                      ON comment.post = post
                     AND comment.status =
                         com.facthub.comment.domain.CommentStatus.ACTIVE
                    WHERE post.status = :status
                      AND (
                            :keyword IS NULL
                            OR LOWER(post.title)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(post.content)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(post.author.nickname)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                      AND (
                            :category IS NULL
                            OR LOWER(post.category)
                                = LOWER(:category)
                      )
                    GROUP BY post
                    ORDER BY (
                        post.viewCount
                        + COUNT(DISTINCT postLike.id) * 3
                        + COUNT(DISTINCT comment.id) * 2
                    ) DESC,
                    post.createdAt DESC,
                    post.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(post)
                    FROM Post post
                    WHERE post.status = :status
                      AND (
                            :keyword IS NULL
                            OR LOWER(post.title)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(post.content)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(post.author.nickname)
                                LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                      AND (
                            :category IS NULL
                            OR LOWER(post.category)
                                = LOWER(:category)
                      )
                    """
    )
    Page<Post> searchPopularPosts(
            @Param("status")
            PostStatus status,
            @Param("keyword")
            String keyword,
            @Param("category")
            String category,
            Pageable pageable
    );

    /*
     * 일반 게시글 상세 조회
     */
    @EntityGraph(attributePaths = "author")
    Optional<Post> findByIdAndStatus(
            Long id,
            PostStatus status
    );

    /*
     * 팩트체크 분석 실행용 게시글 잠금 조회
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "author")
    @Query("""
            SELECT post
            FROM Post post
            WHERE post.id = :postId
              AND post.status = :status
            """)
    Optional<Post> findByIdAndStatusForUpdate(
            @Param("postId")
            Long postId,

            @Param("status")
            PostStatus status
    );
}
