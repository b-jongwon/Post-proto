package com.facthub.comment.service;

import com.facthub.comment.domain.Comment;
import com.facthub.comment.domain.CommentStatus;
import com.facthub.comment.dto.CommentCreateRequest;
import com.facthub.comment.dto.CommentResponse;
import com.facthub.comment.dto.CommentUpdateRequest;
import com.facthub.comment.repository.CommentRepository;
import com.facthub.common.exception.CommentAccessDeniedException;
import com.facthub.common.exception.CommentNotFoundException;
import com.facthub.common.exception.PostNotFoundException;
import com.facthub.common.response.PageResponse;
import com.facthub.post.domain.Post;
import com.facthub.post.domain.PostStatus;
import com.facthub.post.repository.PostRepository;
import com.facthub.notification.service.NotificationService;
import com.facthub.user.domain.User;
import com.facthub.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private static final int MAX_PAGE_SIZE = 50;

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final NotificationService
            notificationService;

    public CommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            UserService userService,
            NotificationService notificationService
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userService = userService;
        this.notificationService =
                notificationService;
    }

    public PageResponse<CommentResponse> getComments(
            Long postId,
            int page,
            int size
    ) {
        validatePagination(page, size);

        findPublishedPost(postId);

        Sort sort = Sort.by(
                Sort.Direction.ASC,
                "createdAt"
        ).and(
                Sort.by(
                        Sort.Direction.ASC,
                        "id"
                )
        );

        PageRequest pageable = PageRequest.of(
                page,
                size,
                sort
        );

        Page<CommentResponse> responsePage =
                commentRepository
                        .findComments(
                                postId,
                                CommentStatus.ACTIVE,
                                pageable
                        )
                        .map(CommentResponse::from);

        return PageResponse.from(responsePage);
    }

    @Transactional
    public CommentResponse create(
            Long postId,
            CommentCreateRequest request,
            String userEmail
    ) {
        Post post = findPublishedPost(postId);

        User user = userService.getActiveUserByEmail(
                userEmail
        );

        Comment comment = Comment.create(
                post,
                user,
                request.content().trim()
        );

        Comment savedComment =
                commentRepository.save(comment);

        notificationService.notifyComment(
                post,
                user
        );

        return CommentResponse.from(savedComment);
    }

    @Transactional
    public CommentResponse update(
            Long postId,
            Long commentId,
            CommentUpdateRequest request,
            String userEmail
    ) {
        findPublishedPost(postId);

        User user = userService.getActiveUserByEmail(
                userEmail
        );

        Comment comment = findActiveComment(
                postId,
                commentId
        );

        validateAuthor(comment, user);

        comment.update(
                request.content().trim()
        );

        return CommentResponse.from(comment);
    }

    @Transactional
    public void delete(
            Long postId,
            Long commentId,
            String userEmail
    ) {
        findPublishedPost(postId);

        User user = userService.getActiveUserByEmail(
                userEmail
        );

        Comment comment = findActiveComment(
                postId,
                commentId
        );

        validateAuthor(comment, user);

        comment.delete();
    }

    private Post findPublishedPost(Long postId) {
        return postRepository
                .findByIdAndStatus(
                        postId,
                        PostStatus.PUBLISHED
                )
                .orElseThrow(PostNotFoundException::new);
    }

    private Comment findActiveComment(
            Long postId,
            Long commentId
    ) {
        return commentRepository
                .findComment(
                        postId,
                        commentId,
                        CommentStatus.ACTIVE
                )
                .orElseThrow(
                        CommentNotFoundException::new
                );
    }

    private void validateAuthor(
            Comment comment,
            User user
    ) {
        if (!comment.isWrittenBy(user.getId())) {
            throw new CommentAccessDeniedException();
        }
    }

    private void validatePagination(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "페이지 번호는 0 이상이어야 합니다."
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "페이지 크기는 1 이상 50 이하여야 합니다."
            );
        }
    }
}
