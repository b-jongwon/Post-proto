package com.facthub.postlike.repository;

import com.facthub.postlike.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PostLikeRepository
        extends JpaRepository<PostLike, Long> {

    boolean existsByPost_IdAndUser_Id(
            Long postId,
            Long userId
    );

    long countByPost_Id(
            Long postId
    );

    long deleteByPost_IdAndUser_Id(
            Long postId,
            Long userId
    );

    /*
     * 게시글 목록에 표시할 좋아요 수를
     * 게시글마다 따로 조회하지 않고 한 번에 집계한다.
     */
    @Query("""
            SELECT
                postLike.post.id AS postId,
                COUNT(postLike.id) AS likeCount
            FROM PostLike postLike
            WHERE postLike.post.id IN :postIds
            GROUP BY postLike.post.id
            """)
    List<PostLikeCountProjection> countLikesByPostIds(
            @Param("postIds")
            Collection<Long> postIds
    );

    interface PostLikeCountProjection {

        Long getPostId();

        Long getLikeCount();
    }
}