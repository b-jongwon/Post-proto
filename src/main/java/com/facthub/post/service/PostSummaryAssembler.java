package com.facthub.post.service;

import com.facthub.comment.domain.CommentStatus;
import com.facthub.comment.repository.CommentRepository;
import com.facthub.factcheck.domain.PostAnalysisSelection;
import com.facthub.factcheck.repository.PostAnalysisSelectionRepository;
import com.facthub.post.domain.Post;
import com.facthub.post.dto.PostSummaryResponse;
import com.facthub.postlike.repository.PostLikeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class PostSummaryAssembler {

    private final PostAnalysisSelectionRepository
            selectionRepository;
    private final PostLikeRepository
            postLikeRepository;
    private final CommentRepository commentRepository;

    public PostSummaryAssembler(
            PostAnalysisSelectionRepository
                    selectionRepository,
            PostLikeRepository postLikeRepository,
            CommentRepository commentRepository
    ) {
        this.selectionRepository =
                selectionRepository;
        this.postLikeRepository =
                postLikeRepository;
        this.commentRepository =
                commentRepository;
    }

    public List<PostSummaryResponse> assemble(
            List<Post> posts
    ) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts
                .stream()
                .map(Post::getId)
                .toList();

        Map<Long, PostAnalysisSelection>
                selectionByPostId =
                selectionRepository
                        .findAllByPost_IdIn(postIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        PostAnalysisSelection
                                                ::getPostId,
                                        Function.identity()
                                )
                        );

        Map<Long, Long> likeCountByPostId =
                postLikeRepository
                        .countLikesByPostIds(postIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        PostLikeRepository
                                                .PostLikeCountProjection
                                                ::getPostId,
                                        PostLikeRepository
                                                .PostLikeCountProjection
                                                ::getLikeCount
                                )
                        );

        Map<Long, Long> commentCountByPostId =
                commentRepository
                        .countCommentsByPostIds(
                                postIds,
                                CommentStatus.ACTIVE
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        CommentRepository
                                                .CommentCountProjection
                                                ::getPostId,
                                        CommentRepository
                                                .CommentCountProjection
                                                ::getCommentCount
                                )
                        );

        return posts
                .stream()
                .map(post ->
                        PostSummaryResponse.from(
                                post,
                                selectionByPostId.get(
                                        post.getId()
                                ),
                                likeCountByPostId
                                        .getOrDefault(
                                                post.getId(),
                                                0L
                                        ),
                                commentCountByPostId
                                        .getOrDefault(
                                                post.getId(),
                                                0L
                                        )
                        )
                )
                .toList();
    }
}

