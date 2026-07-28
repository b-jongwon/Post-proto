package com.facthub.comment.controller;

import com.facthub.comment.dto.CommentCreateRequest;
import com.facthub.comment.dto.CommentResponse;
import com.facthub.comment.dto.CommentUpdateRequest;
import com.facthub.comment.service.CommentService;
import com.facthub.common.response.ApiResponse;
import com.facthub.common.response.PageResponse;
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
@RequestMapping(
        "/api/posts/{postId}/comments"
)
public class CommentController {

    private final CommentService commentService;

    public CommentController(
            CommentService commentService
    ) {
        this.commentService = commentService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CommentResponse>>
    getComments(
            @PathVariable Long postId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {
        PageResponse<CommentResponse> response =
                commentService.getComments(
                        postId,
                        page,
                        size
                );

        return ApiResponse.success(response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> create(
            @PathVariable Long postId,

            @Valid @RequestBody
            CommentCreateRequest request,

            Authentication authentication
    ) {
        CommentResponse response =
                commentService.create(
                        postId,
                        request,
                        authentication.getName()
                );

        return ApiResponse.success(response);
    }

    @PutMapping("/{commentId}")
    public ApiResponse<CommentResponse> update(
            @PathVariable Long postId,
            @PathVariable Long commentId,

            @Valid @RequestBody
            CommentUpdateRequest request,

            Authentication authentication
    ) {
        CommentResponse response =
                commentService.update(
                        postId,
                        commentId,
                        request,
                        authentication.getName()
                );

        return ApiResponse.success(response);
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Map<String, String>> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        commentService.delete(
                postId,
                commentId,
                authentication.getName()
        );

        return ApiResponse.success(
                Map.of(
                        "message",
                        "댓글이 삭제되었습니다."
                )
        );
    }
}