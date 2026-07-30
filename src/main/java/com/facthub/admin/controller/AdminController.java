package com.facthub.admin.controller;

import com.facthub.admin.dto.AdminDashboardResponse;
import com.facthub.admin.dto.AdminPostResponse;
import com.facthub.admin.dto.AdminPostVisibilityRequest;
import com.facthub.admin.dto.AdminUserResponse;
import com.facthub.admin.dto.AdminUserStatusRequest;
import com.facthub.admin.service.AdminService;
import com.facthub.common.response.ApiResponse;
import com.facthub.common.response.PageResponse;
import com.facthub.factcheck.dto.FactCheckResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse>
    getDashboard() {
        return ApiResponse.success(
                adminService.getDashboard()
        );
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminUserResponse>>
    getUsers(
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size
    ) {
        return ApiResponse.success(
                adminService.getUsers(page, size)
        );
    }

    @PutMapping("/users/{userId}/status")
    public ApiResponse<AdminUserResponse>
    changeUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody
            AdminUserStatusRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(
                adminService.changeUserStatus(
                        userId,
                        request,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<AdminPostResponse>>
    getPosts(
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size
    ) {
        return ApiResponse.success(
                adminService.getPosts(page, size)
        );
    }

    @PutMapping("/posts/{postId}/visibility")
    public ApiResponse<AdminPostResponse>
    changePostVisibility(
            @PathVariable Long postId,
            @Valid @RequestBody
            AdminPostVisibilityRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(
                adminService.changePostVisibility(
                        postId,
                        request,
                        authentication.getName()
                )
        );
    }

    @PostMapping("/posts/{postId}/analyses")
    public ApiResponse<FactCheckResponse>
    forceAnalysis(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        return ApiResponse.success(
                adminService.forceAnalysis(
                        postId,
                        authentication.getName()
                )
        );
    }
}

