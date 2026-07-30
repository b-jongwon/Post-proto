package com.facthub.user.service;

import com.facthub.comment.domain.Comment;
import com.facthub.comment.domain.CommentStatus;
import com.facthub.comment.repository.CommentRepository;
import com.facthub.notification.repository.NotificationRepository;
import com.facthub.post.domain.Post;
import com.facthub.post.domain.PostStatus;
import com.facthub.post.repository.PostRepository;
import com.facthub.post.service.PostSummaryAssembler;
import com.facthub.postlike.domain.PostLike;
import com.facthub.postlike.repository.PostLikeRepository;
import com.facthub.user.domain.User;
import com.facthub.user.dto.MyCommentActivityResponse;
import com.facthub.user.dto.MyDashboardResponse;
import com.facthub.user.dto.MyInfoResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserActivityService {

    private static final int ACTIVITY_LIMIT = 20;

    private final UserService userService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository
            postLikeRepository;
    private final NotificationRepository
            notificationRepository;
    private final PostSummaryAssembler
            postSummaryAssembler;

    public UserActivityService(
            UserService userService,
            PostRepository postRepository,
            CommentRepository commentRepository,
            PostLikeRepository postLikeRepository,
            NotificationRepository
                    notificationRepository,
            PostSummaryAssembler postSummaryAssembler
    ) {
        this.userService = userService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository =
                postLikeRepository;
        this.notificationRepository =
                notificationRepository;
        this.postSummaryAssembler =
                postSummaryAssembler;
    }

    public MyDashboardResponse getDashboard(
            String userEmail
    ) {
        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        PageRequest limit =
                PageRequest.of(
                        0,
                        ACTIVITY_LIMIT
                );

        List<Post> posts =
                postRepository.findRecentByAuthor(
                        user.getId(),
                        PostStatus.DELETED,
                        limit
                );

        List<Comment> comments =
                commentRepository
                        .findRecentByAuthor(
                                user.getId(),
                                CommentStatus.ACTIVE,
                                limit
                        );

        List<Post> likedPosts =
                postLikeRepository
                        .findRecentByUser(
                                user.getId(),
                                limit
                        )
                        .stream()
                        .map(PostLike::getPost)
                        .toList();

        return new MyDashboardResponse(
                MyInfoResponse.from(user),
                postRepository
                        .countByAuthor_IdAndStatusNot(
                                user.getId(),
                                PostStatus.DELETED
                        ),
                commentRepository
                        .countByAuthor_IdAndStatus(
                                user.getId(),
                                CommentStatus.ACTIVE
                        ),
                postLikeRepository
                        .countByUser_Id(
                                user.getId()
                        ),
                notificationRepository
                        .countByRecipient_IdAndReadFalse(
                                user.getId()
                        ),
                postSummaryAssembler.assemble(posts),
                comments
                        .stream()
                        .map(
                                MyCommentActivityResponse
                                        ::from
                        )
                        .toList(),
                postSummaryAssembler
                        .assemble(likedPosts)
        );
    }
}

