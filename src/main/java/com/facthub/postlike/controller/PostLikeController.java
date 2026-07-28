package com.facthub.postlike.controller;

import com.facthub.common.response.ApiResponse;
import com.facthub.postlike.dto.PostLikeResponse;
import com.facthub.postlike.service.PostLikeService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/posts/{postId}/likes"
)
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(
            PostLikeService postLikeService
    ) {
        this.postLikeService = postLikeService;
    }

    /*
     * 좋아요 수와 현재 사용자의 좋아요 여부 조회
     *
     * 비회원도 조회할 수 있다.
     */
    @GetMapping
    public ApiResponse<PostLikeResponse>
    getLikeStatus(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        String userEmail =
                getAuthenticatedEmail(
                        authentication
                );

        PostLikeResponse response =
                postLikeService.getLikeStatus(
                        postId,
                        userEmail
                );

        return ApiResponse.success(response);
    }

    /*
     * 좋아요
     *
     * 로그인과 CSRF 토큰이 필요하다.
     */
    @PostMapping
    public ApiResponse<PostLikeResponse> like(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        PostLikeResponse response =
                postLikeService.like(
                        postId,
                        authentication.getName()
                );

        return ApiResponse.success(response);
    }

    /*
     * 좋아요 취소
     *
     * 로그인과 CSRF 토큰이 필요하다.
     */
    @DeleteMapping
    public ApiResponse<PostLikeResponse> unlike(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        PostLikeResponse response =
                postLikeService.unlike(
                        postId,
                        authentication.getName()
                );

        return ApiResponse.success(response);
    }

    /*
     * 비로그인 요청에서는 null을 반환한다.
     */
    private String getAuthenticatedEmail(
            Authentication authentication
    ) {
        if (authentication == null) {
            return null;
        }

        if (authentication
                instanceof AnonymousAuthenticationToken) {
            return null;
        }

        if (!authentication.isAuthenticated()) {
            return null;
        }

        return authentication.getName();
    }
}