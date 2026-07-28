package com.facthub.comment.repository;

import com.facthub.comment.domain.Comment;
import com.facthub.comment.domain.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository
        extends JpaRepository<Comment, Long> {

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
}