package com.facthub.post.service;

import com.facthub.common.exception.PostAccessDeniedException;
import com.facthub.common.exception.PostNotFoundException;
import com.facthub.common.response.PageResponse;
import com.facthub.factcheck.domain.FactCheckStatus;
import com.facthub.factcheck.domain.PostAnalysisSelection;
import com.facthub.factcheck.repository.FactCheckAnalysisRepository;
import com.facthub.factcheck.repository.PostAnalysisSelectionRepository;
import com.facthub.post.domain.Post;
import com.facthub.post.domain.PostStatus;
import com.facthub.post.dto.PostCreateRequest;
import com.facthub.post.dto.PostResponse;
import com.facthub.post.dto.PostStatisticsResponse;
import com.facthub.post.dto.PostSummaryResponse;
import com.facthub.post.dto.PostUpdateRequest;
import com.facthub.post.repository.PostRepository;
import com.facthub.user.domain.User;
import com.facthub.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final int MAX_CATEGORY_LENGTH = 50;

    private final PostRepository postRepository;
    private final UserService userService;
    private final FactCheckAnalysisRepository
            factCheckAnalysisRepository;
    private final PostAnalysisSelectionRepository
            selectionRepository;

    public PostService(
            PostRepository postRepository,
            UserService userService,
            FactCheckAnalysisRepository
                    factCheckAnalysisRepository,
            PostAnalysisSelectionRepository
                    selectionRepository
    ) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.factCheckAnalysisRepository =
                factCheckAnalysisRepository;
        this.selectionRepository =
                selectionRepository;
    }

    @Transactional
    public PostResponse create(
            PostCreateRequest request,
            String userEmail
    ) {
        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        Post post = Post.create(
                user,
                request.title().trim(),
                request.content().trim(),
                request.category().trim()
        );

        Post savedPost =
                postRepository.save(post);

        return PostResponse.from(savedPost);
    }

    /*
     * 게시글 목록 조회
     *
     * 게시글 페이지를 먼저 조회한 뒤,
     * 해당 페이지의 대표 분석을 한 번에 가져와
     * 홈 카드에 분석 상태와 결과를 함께 내려준다.
     */
    public PageResponse<PostSummaryResponse> getPosts(
            int page,
            int size,
            String keyword,
            String category,
            String sort
    ) {
        validatePagination(page, size);

        String normalizedKeyword =
                normalizeOptionalValue(keyword);

        String normalizedCategory =
                normalizeOptionalValue(category);

        validateSearchCondition(
                normalizedKeyword,
                normalizedCategory
        );

        PageRequest pageable = PageRequest.of(
                page,
                size,
                createSort(sort)
        );

        Page<Post> postPage =
                postRepository.searchPosts(
                        PostStatus.PUBLISHED,
                        normalizedKeyword,
                        normalizedCategory,
                        pageable
                );

        List<Long> postIds =
                postPage.getContent()
                        .stream()
                        .map(Post::getId)
                        .toList();

        Map<Long, PostAnalysisSelection>
                selectionByPostId;

        if (postIds.isEmpty()) {
            selectionByPostId = Map.of();
        } else {
            selectionByPostId =
                    selectionRepository
                            .findAllByPost_IdIn(
                                    postIds
                            )
                            .stream()
                            .collect(
                                    Collectors.toMap(
                                            PostAnalysisSelection::getPostId,
                                            Function.identity()
                                    )
                            );
        }

        Page<PostSummaryResponse> responsePage =
                postPage.map(post ->
                        PostSummaryResponse.from(
                                post,
                                selectionByPostId.get(
                                        post.getId()
                                )
                        )
                );

        return PageResponse.from(responsePage);
    }

    /*
     * 홈 통계 조회
     *
     * 완료된 검증은 대표 분석이 존재하면서
     * COMPLETED이고 stale이 아닌 게시글만 센다.
     *
     * 검증 대기 = 전체 공개 게시글 - 유효한 검증 완료 게시글
     */
    public PostStatisticsResponse getStatistics() {
        long totalPostCount =
                postRepository.countByStatus(
                        PostStatus.PUBLISHED
                );

        long completedVerificationCount =
                selectionRepository
                        .countCurrentVerifiedPosts(
                                PostStatus.PUBLISHED,
                                FactCheckStatus.COMPLETED
                        );

        return PostStatisticsResponse.of(
                totalPostCount,
                completedVerificationCount
        );
    }

    @Transactional
    public PostResponse getPost(Long postId) {
        Post post = findPublishedPost(postId);

        post.increaseViewCount();

        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse update(
            Long postId,
            PostUpdateRequest request,
            String userEmail
    ) {
        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        Post post = findPublishedPost(postId);

        validateAuthor(post, user);

        String newTitle =
                request.title().trim();

        String newContent =
                request.content().trim();

        String newCategory =
                request.category().trim();

        boolean analysisTargetChanged =
                !post.getTitle().equals(newTitle)
                        || !post.getContent().equals(
                        newContent
                );

        post.update(
                newTitle,
                newContent,
                newCategory
        );

        if (analysisTargetChanged) {
            factCheckAnalysisRepository
                    .markAllStaleByPostId(
                            postId
                    );
        }

        return PostResponse.from(post);
    }

    @Transactional
    public void delete(
            Long postId,
            String userEmail
    ) {
        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        Post post = findPublishedPost(postId);

        validateAuthor(post, user);

        post.delete();
    }

    private Post findPublishedPost(
            Long postId
    ) {
        return postRepository
                .findByIdAndStatus(
                        postId,
                        PostStatus.PUBLISHED
                )
                .orElseThrow(
                        PostNotFoundException::new
                );
    }

    private void validateAuthor(
            Post post,
            User user
    ) {
        if (!post.isWrittenBy(user.getId())) {
            throw new PostAccessDeniedException();
        }
    }

    private String normalizeOptionalValue(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private Sort createSort(String sort) {
        String normalizedSort =
                normalizeSortValue(sort);

        return switch (normalizedSort) {
            case "latest" ->
                    Sort.by(
                            Sort.Direction.DESC,
                            "createdAt"
                    ).and(
                            Sort.by(
                                    Sort.Direction.DESC,
                                    "id"
                            )
                    );

            case "views" ->
                    Sort.by(
                            Sort.Direction.DESC,
                            "viewCount"
                    ).and(
                            Sort.by(
                                    Sort.Direction.DESC,
                                    "createdAt"
                            )
                    ).and(
                            Sort.by(
                                    Sort.Direction.DESC,
                                    "id"
                            )
                    );

            default ->
                    throw new IllegalArgumentException(
                            "정렬 방식은 latest 또는 views만 가능합니다."
                    );
        };
    }

    private String normalizeSortValue(
            String sort
    ) {
        if (!StringUtils.hasText(sort)) {
            return "latest";
        }

        return sort
                .trim()
                .toLowerCase(Locale.ROOT);
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

    private void validateSearchCondition(
            String keyword,
            String category
    ) {
        if (keyword != null
                && keyword.length()
                > MAX_KEYWORD_LENGTH) {

            throw new IllegalArgumentException(
                    "검색어는 100자 이하여야 합니다."
            );
        }

        if (category != null
                && category.length()
                > MAX_CATEGORY_LENGTH) {

            throw new IllegalArgumentException(
                    "카테고리는 50자 이하여야 합니다."
            );
        }
    }
}