package com.facthub.postlike.service;

import com.facthub.common.exception.PostNotFoundException;
import com.facthub.post.domain.Post;
import com.facthub.post.domain.PostStatus;
import com.facthub.post.repository.PostRepository;
import com.facthub.postlike.domain.PostLike;
import com.facthub.postlike.dto.PostLikeResponse;
import com.facthub.postlike.repository.PostLikeRepository;
import com.facthub.user.domain.User;
import com.facthub.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    public PostLikeService(
            PostLikeRepository postLikeRepository,
            PostRepository postRepository,
            UserService userService
    ) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
        this.userService = userService;
    }

    /*
     * 좋아요 상태 조회
     *
     * 비로그인 사용자는 liked=false로 반환한다.
     */
    public PostLikeResponse getLikeStatus(
            Long postId,
            String userEmail
    ) {
        findPublishedPost(postId);

        long likeCount =
                postLikeRepository.countByPost_Id(
                        postId
                );

        boolean liked = false;

        if (userEmail != null) {
            User user =
                    userService.getActiveUserByEmail(
                            userEmail
                    );

            liked =
                    postLikeRepository
                            .existsByPost_IdAndUser_Id(
                                    postId,
                                    user.getId()
                            );
        }

        return new PostLikeResponse(
                postId,
                likeCount,
                liked
        );
    }

    /*
     * 게시글 좋아요
     *
     * 이미 좋아요한 경우 중복 저장하지 않고
     * 현재 상태를 그대로 반환한다.
     */
    @Transactional
    public PostLikeResponse like(
            Long postId,
            String userEmail
    ) {
        Post post = findPublishedPost(postId);

        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        boolean alreadyLiked =
                postLikeRepository
                        .existsByPost_IdAndUser_Id(
                                postId,
                                user.getId()
                        );

        if (!alreadyLiked) {
            PostLike postLike =
                    PostLike.create(
                            post,
                            user
                    );

            postLikeRepository.save(postLike);
        }

        long likeCount =
                postLikeRepository.countByPost_Id(
                        postId
                );

        return new PostLikeResponse(
                postId,
                likeCount,
                true
        );
    }

    /*
     * 게시글 좋아요 취소
     *
     * 이미 취소된 상태에서도 오류를 발생시키지 않는다.
     */
    @Transactional
    public PostLikeResponse unlike(
            Long postId,
            String userEmail
    ) {
        findPublishedPost(postId);

        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        postLikeRepository
                .deleteByPost_IdAndUser_Id(
                        postId,
                        user.getId()
                );

        long likeCount =
                postLikeRepository.countByPost_Id(
                        postId
                );

        return new PostLikeResponse(
                postId,
                likeCount,
                false
        );
    }

    private Post findPublishedPost(Long postId) {
        return postRepository
                .findByIdAndStatus(
                        postId,
                        PostStatus.PUBLISHED
                )
                .orElseThrow(PostNotFoundException::new);
    }
}