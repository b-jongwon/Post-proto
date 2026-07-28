package com.facthub.post.service;

import com.facthub.common.exception.PostAccessDeniedException;
import com.facthub.common.exception.PostNotFoundException;
import com.facthub.common.response.PageResponse;
import com.facthub.post.domain.Post;
import com.facthub.post.domain.PostStatus;
import com.facthub.post.dto.PostCreateRequest;
import com.facthub.post.dto.PostResponse;
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
import com.facthub.factcheck.repository.FactCheckAnalysisRepository;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final int MAX_CATEGORY_LENGTH = 50;

    private final PostRepository postRepository;
    private final UserService userService;
    private final FactCheckAnalysisRepository factCheckAnalysisRepository;

    public PostService(
            PostRepository postRepository,
            UserService userService,
            FactCheckAnalysisRepository
                    factCheckAnalysisRepository
    ) {
        this.postRepository = postRepository;
        this.userService = userService;

        this.factCheckAnalysisRepository =
                factCheckAnalysisRepository;
    }

    /*
     * 게시글 작성
     */
    @Transactional
    public PostResponse create(
            PostCreateRequest request,
            String userEmail
    ) {
        User user = userService.getActiveUserByEmail(
                userEmail
        );

        Post post = Post.create(
                user,
                request.title().trim(),
                request.content().trim(),
                request.category().trim()
        );

        Post savedPost = postRepository.save(post);

        return PostResponse.from(savedPost);
    }

    /*
     * 게시글 목록 조회
     *
     * keyword:
     * - 제목
     * - 본문
     * - 작성자 닉네임
     *
     * category:
     * - 게시글 카테고리
     *
     * sort:
     * - latest: 최신순
     * - views: 조회수순
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

        Sort postSort = createSort(sort);

        PageRequest pageable = PageRequest.of(
                page,
                size,
                postSort
        );

        Page<PostSummaryResponse> responsePage =
                postRepository
                        .searchPosts(
                                PostStatus.PUBLISHED,
                                normalizedKeyword,
                                normalizedCategory,
                                pageable
                        )
                        .map(PostSummaryResponse::from);

        return PageResponse.from(responsePage);
    }

    /*
     * 게시글 상세 조회
     *
     * 상세 조회가 성공하면 조회수를 1 증가시킨다.
     */
    @Transactional
    public PostResponse getPost(Long postId) {

        Post post = findPublishedPost(postId);

        post.increaseViewCount();

        return PostResponse.from(post);
    }

    /*
     * 게시글 수정
     */
    @Transactional
    public PostResponse update(
            Long postId,
            PostUpdateRequest request,
            String userEmail
    ) {
        User user =
                userService
                        .getActiveUserByEmail(
                                userEmail
                        );

        Post post =
                findPublishedPost(postId);

        validateAuthor(
                post,
                user
        );

        String newTitle =
                request.title().trim();

        String newContent =
                request.content().trim();

        String newCategory =
                request.category().trim();

        /*
         * 팩트체크 분석 대상은 제목과 본문이다.
         *
         * 카테고리만 수정한 경우에는
         * 기존 분석을 stale 처리하지 않는다.
         */
        boolean analysisTargetChanged =
                !post.getTitle().equals(
                        newTitle
                )
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

    /*
     * 게시글 소프트 삭제
     */
    @Transactional
    public void delete(
            Long postId,
            String userEmail
    ) {
        User user = userService.getActiveUserByEmail(
                userEmail
        );

        Post post = findPublishedPost(postId);

        validateAuthor(post, user);

        post.delete();
    }

    /*
     * 공개 상태 게시글 조회
     */
    private Post findPublishedPost(Long postId) {
        return postRepository
                .findByIdAndStatus(
                        postId,
                        PostStatus.PUBLISHED
                )
                .orElseThrow(PostNotFoundException::new);
    }

    /*
     * 게시글 작성자 검증
     */
    private void validateAuthor(
            Post post,
            User user
    ) {
        if (!post.isWrittenBy(user.getId())) {
            throw new PostAccessDeniedException();
        }
    }

    /*
     * 검색값 정규화
     *
     * null, 빈 문자열, 공백만 있는 문자열은
     * 검색 조건이 없는 것으로 처리한다.
     */
    private String normalizeOptionalValue(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    /*
     * 정렬 조건 생성
     */
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

    /*
     * 정렬값 정규화
     */
    private String normalizeSortValue(String sort) {

        if (!StringUtils.hasText(sort)) {
            return "latest";
        }

        return sort
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    /*
     * 페이지 번호와 크기 검증
     */
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

    /*
     * 검색 조건 길이 검증
     */
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