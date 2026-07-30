package com.facthub.user.controller;

import com.facthub.common.response.ApiResponse;
import com.facthub.user.dto.MyDashboardResponse;
import com.facthub.user.service.UserActivityService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class ProfileController {

    private final UserActivityService
            userActivityService;

    public ProfileController(
            UserActivityService userActivityService
    ) {
        this.userActivityService =
                userActivityService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<MyDashboardResponse>
    getDashboard(Authentication authentication) {
        return ApiResponse.success(
                userActivityService.getDashboard(
                        authentication.getName()
                )
        );
    }
}

