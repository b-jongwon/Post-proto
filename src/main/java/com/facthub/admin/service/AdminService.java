package com.facthub.admin.service;

import com.facthub.admin.domain.AdminAction;
import com.facthub.admin.dto.AdminDashboardResponse;
import com.facthub.admin.dto.AdminPostResponse;
import com.facthub.admin.dto.AdminPostVisibilityRequest;
import com.facthub.admin.dto.AdminUserResponse;
import com.facthub.admin.dto.AdminUserStatusRequest;
import com.facthub.admin.repository.AdminActionRepository;
import com.facthub.comment.domain.CommentStatus;
import com.facthub.comment.repository.CommentRepository;
import com.facthub.common.exception.BusinessException;
import com.facthub.common.response.PageResponse;
import com.facthub.factcheck.dto.FactCheckResponse;
import com.facthub.factcheck.service.FactCheckService;
import com.facthub.post.domain.Post;
import com.facthub.post.domain.PostStatus;
import com.facthub.post.dto.PostSummaryResponse;
import com.facthub.post.repository.PostRepository;
import com.facthub.post.service.PostSummaryAssembler;
import com.facthub.postlike.repository.PostLikeRepository;
import com.facthub.user.domain.User;
import com.facthub.user.domain.UserStatus;
import com.facthub.user.repository.UserRepository;
import com.facthub.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository
            postLikeRepository;
    private final PostSummaryAssembler
            postSummaryAssembler;
    private final AdminActionRepository
            adminActionRepository;
    private final UserService userService;
    private final FactCheckService factCheckService;

    public AdminService(
            UserRepository userRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            PostLikeRepository postLikeRepository,
            PostSummaryAssembler postSummaryAssembler,
            AdminActionRepository
                    adminActionRepository,
            UserService userService,
            FactCheckService factCheckService
    ) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository =
                postLikeRepository;
        this.postSummaryAssembler =
                postSummaryAssembler;
        this.adminActionRepository =
                adminActionRepository;
        this.userService = userService;
        this.factCheckService = factCheckService;
    }

    public AdminDashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end =
                today.plusDays(1).atStartOfDay();

        return new AdminDashboardResponse(
                userRepository.count(),
                userRepository.countByStatus(
                        UserStatus.ACTIVE
                ),
                userRepository.countByStatus(
                        UserStatus.SUSPENDED
                ),
                postRepository.countByStatus(
                        PostStatus.PUBLISHED
                ),
                postRepository.countByStatus(
                        PostStatus.HIDDEN
                ),
                postLikeRepository.count(),
                commentRepository.countByStatus(
                        CommentStatus.ACTIVE
                ),
                postRepository
                        .countByStatusAndCreatedAtBetween(
                                PostStatus.PUBLISHED,
                                start,
                                end
                        ),
                commentRepository
                        .countByStatusAndCreatedAtBetween(
                                CommentStatus.ACTIVE,
                                start,
                                end
                        )
        );
    }

    public PageResponse<AdminUserResponse> getUsers(
            int page,
            int size
    ) {
        PageRequest pageable =
                validatePage(page, size);

        return PageResponse.from(
                userRepository
                        .findAllByOrderByCreatedAtDesc(
                                pageable
                        )
                        .map(AdminUserResponse::from)
        );
    }

    public PageResponse<AdminPostResponse> getPosts(
            int page,
            int size
    ) {
        PageRequest pageable =
                validatePage(page, size);

        Page<Post> posts =
                postRepository.findAdminPosts(
                        PostStatus.DELETED,
                        pageable
                );

        List<PostSummaryResponse> summaries =
                postSummaryAssembler.assemble(
                        posts.getContent()
                );

        List<AdminPostResponse> responses =
                new ArrayList<>(summaries.size());

        for (int index = 0;
             index < summaries.size();
             index++) {

            responses.add(
                    AdminPostResponse.from(
                            posts.getContent().get(index),
                            summaries.get(index)
                    )
            );
        }

        Page<AdminPostResponse> responsePage =
                new PageImpl<>(
                        responses,
                        pageable,
                        posts.getTotalElements()
                );

        return PageResponse.from(responsePage);
    }

    @Transactional
    public AdminUserResponse changeUserStatus(
            Long userId,
            AdminUserStatusRequest request,
            String adminEmail
    ) {
        User admin = getAdmin(adminEmail);

        User target = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                "USER_NOT_FOUND",
                                "회원을 찾을 수 없습니다.",
                                HttpStatus.NOT_FOUND
                        )
                );

        if (target.isAdmin()) {
            throw new BusinessException(
                    "ADMIN_STATUS_PROTECTED",
                    "관리자 계정 상태는 이 화면에서 변경할 수 없습니다.",
                    HttpStatus.FORBIDDEN
            );
        }

        if (request.status() == UserStatus.ACTIVE) {
            target.activate();
        } else if (
                request.status()
                        == UserStatus.SUSPENDED
        ) {
            target.suspend();
        } else {
            throw new IllegalArgumentException(
                    "회원 상태는 ACTIVE 또는 SUSPENDED만 가능합니다."
            );
        }

        recordAction(
                admin,
                "USER_STATUS_CHANGED",
                "USER",
                target.getId(),
                "회원 상태를 %s로 변경"
                        .formatted(
                                target.getStatus().name()
                        )
        );

        return AdminUserResponse.from(target);
    }

    @Transactional
    public AdminPostResponse changePostVisibility(
            Long postId,
            AdminPostVisibilityRequest request,
            String adminEmail
    ) {
        User admin = getAdmin(adminEmail);

        Post post = postRepository
                .findById(postId)
                .filter(candidate ->
                        candidate.getStatus()
                                != PostStatus.DELETED
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "POST_NOT_FOUND",
                                "게시글을 찾을 수 없습니다.",
                                HttpStatus.NOT_FOUND
                        )
                );

        if (Boolean.TRUE.equals(request.hidden())) {
            post.hide();
        } else {
            post.publish();
        }

        recordAction(
                admin,
                "POST_VISIBILITY_CHANGED",
                "POST",
                post.getId(),
                "게시글 상태를 %s로 변경"
                        .formatted(
                                post.getStatus().name()
                        )
        );

        PostSummaryResponse summary =
                postSummaryAssembler
                        .assemble(List.of(post))
                        .getFirst();

        return AdminPostResponse.from(
                post,
                summary
        );
    }

    @Transactional(
            propagation = Propagation.NOT_SUPPORTED
    )
    public FactCheckResponse forceAnalysis(
            Long postId,
            String adminEmail
    ) {
        User admin = getAdmin(adminEmail);

        FactCheckResponse response =
                factCheckService.analyze(
                        postId,
                        adminEmail
                );

        recordCompletedAnalysisAction(
                admin,
                postId,
                response.analysisId()
        );

        return response;
    }

    @Transactional
    protected void recordCompletedAnalysisAction(
            User admin,
            Long postId,
            Long analysisId
    ) {
        recordAction(
                admin,
                "FACT_CHECK_FORCED",
                "POST",
                postId,
                "AI 분석 #%d 강제 실행"
                        .formatted(analysisId)
        );
    }

    private User getAdmin(String adminEmail) {
        User admin =
                userService.getActiveUserByEmail(
                        adminEmail
                );

        if (!admin.isAdmin()) {
            throw new BusinessException(
                    "ADMIN_REQUIRED",
                    "관리자 권한이 필요합니다.",
                    HttpStatus.FORBIDDEN
            );
        }

        return admin;
    }

    private void recordAction(
            User admin,
            String actionType,
            String targetType,
            Long targetId,
            String description
    ) {
        adminActionRepository.save(
                AdminAction.create(
                        admin,
                        actionType,
                        targetType,
                        targetId,
                        description
                )
        );
    }

    private PageRequest validatePage(
            int page,
            int size
    ) {
        if (page < 0
                || size < 1
                || size > MAX_PAGE_SIZE) {

            throw new IllegalArgumentException(
                    "관리자 목록 페이지 범위를 확인해주세요."
            );
        }

        return PageRequest.of(page, size);
    }
}
