package com.facthub.comment.repository;

import com.facthub.comment.domain.Comment;
import com.facthub.comment.domain.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    long countByPost_IdAndStatus(
            Long postId,
            CommentStatus status
    );

    long countByAuthor_IdAndStatus(
            Long authorId,
            CommentStatus status
    );

    long countByStatus(CommentStatus status);

    long countByStatusAndCreatedAtBetween(
            CommentStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            SELECT
                comment.post.id AS postId,
                COUNT(comment.id) AS commentCount
            FROM Comment comment
            WHERE comment.post.id IN :postIds
              AND comment.status = :status
            GROUP BY comment.post.id
            """)
    List<CommentCountProjection> countCommentsByPostIds(
            @Param("postIds")
            Collection<Long> postIds,
            @Param("status")
            CommentStatus status
    );

    @EntityGraph(
            attributePaths = {
                    "author",
                    "post",
                    "post.author"
            }
    )
    @Query("""
            SELECT comment
            FROM Comment comment
            WHERE comment.author.id = :authorId
              AND comment.status = :status
            ORDER BY comment.createdAt DESC,
                     comment.id DESC
            """)
    List<Comment> findRecentByAuthor(
            @Param("authorId")
            Long authorId,
            @Param("status")
            CommentStatus status,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "author",
                    "post"
            }
    )
    @Query("""
            SELECT comment
            FROM Comment comment
            WHERE comment.post.id = :postId
              AND comment.status = :status
            """)
    Page<Comment> findComments(
            @Param("postId")
            Long postId,

            @Param("status")
            CommentStatus status,

            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "author",
                    "post"
            }
    )
    @Query("""
            SELECT comment
            FROM Comment comment
            WHERE comment.id = :commentId
              AND comment.post.id = :postId
              AND comment.status = :status
            """)
    Optional<Comment> findComment(
            @Param("postId")
            Long postId,

            @Param("commentId")
            Long commentId,

            @Param("status")
            CommentStatus status
    );

    interface CommentCountProjection {

        Long getPostId();

        Long getCommentCount();
    }
}
