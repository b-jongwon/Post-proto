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
     * 일반 게시글 상세 조회
     *
     * 목록·상세·수정·삭제 등
     * 일반적인 게시글 기능에서 사용한다.
     */
    @EntityGraph(attributePaths = "author")
    Optional<Post> findByIdAndStatus(
            Long id,
            PostStatus status
    );

    /*
     * 팩트체크 분석 실행용 게시글 잠금 조회
     *
     * 같은 게시글에 여러 분석 요청이 동시에 들어와
     * 동일한 runNumber가 생성되는 것을 방지한다.
     *
     * 반드시 @Transactional 메서드 안에서 호출한다.
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