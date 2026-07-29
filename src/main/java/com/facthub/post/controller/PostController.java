package com.facthub.post.controller;

import com.facthub.common.response.ApiResponse;
import com.facthub.common.response.PageResponse;
import com.facthub.post.dto.PostCreateRequest;
import com.facthub.post.dto.PostResponse;
import com.facthub.post.dto.PostStatisticsResponse;
import com.facthub.post.dto.PostSummaryResponse;
import com.facthub.post.dto.PostUpdateRequest;
import com.facthub.post.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(
            PostService postService
    ) {
        this.postService = postService;
    }

    @GetMapping
    public ApiResponse<PageResponse<PostSummaryResponse>>
    getPosts(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            String category,

            @RequestParam(defaultValue = "latest")
            String sort
    ) {
        PageResponse<PostSummaryResponse> response =
                postService.getPosts(
                        page,
                        size,
                        keyword,
                        category,
                        sort
                );

        return ApiResponse.success(response);
    }

    /*
     * 홈 화면용 게시글·검증 통계
     */
    @GetMapping("/statistics")
    public ApiResponse<PostStatisticsResponse>
    getStatistics() {
        return ApiResponse.success(
                postService.getStatistics()
        );
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(
            @PathVariable Long postId
    ) {
        return ApiResponse.success(
                postService.getPost(postId)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> create(
            @Valid @RequestBody
            PostCreateRequest request,

            Authentication authentication
    ) {
        PostResponse response =
                postService.create(
                        request,
                        authentication.getName()
                );

        return ApiResponse.success(response);
    }

    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> update(
            @PathVariable Long postId,

            @Valid @RequestBody
            PostUpdateRequest request,

            Authentication authentication
    ) {
        PostResponse response =
                postService.update(
                        postId,
                        request,
                        authentication.getName()
                );

        return ApiResponse.success(response);
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Map<String, String>> delete(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        postService.delete(
                postId,
                authentication.getName()
        );

        return ApiResponse.success(
                Map.of(
                        "message",
                        "게시글이 삭제되었습니다."
                )
        );
    }
}