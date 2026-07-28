package com.facthub.postlike.repository;

import com.facthub.postlike.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

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
}