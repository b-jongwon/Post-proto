package com.facthub.notification.controller;

import com.facthub.common.response.ApiResponse;
import com.facthub.common.response.PageResponse;
import com.facthub.notification.dto.NotificationResponse;
import com.facthub.notification.dto.UnreadNotificationCountResponse;
import com.facthub.notification.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService
            notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService =
                notificationService;
    }

    @GetMapping
    public ApiResponse<
            PageResponse<NotificationResponse>>
    getNotifications(
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size,
            Authentication authentication
    ) {
        return ApiResponse.success(
                notificationService
                        .getNotifications(
                                authentication.getName(),
                                page,
                                size
                        )
        );
    }

    @GetMapping("/unread-count")
    public ApiResponse<
            UnreadNotificationCountResponse>
    getUnreadCount(
            Authentication authentication
    ) {
        return ApiResponse.success(
                notificationService
                        .getUnreadCount(
                                authentication.getName()
                        )
        );
    }

    @PostMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse>
    markRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        return ApiResponse.success(
                notificationService.markRead(
                        notificationId,
                        authentication.getName()
                )
        );
    }

    @PostMapping("/read-all")
    public ApiResponse<Map<String, String>>
    markAllRead(
            Authentication authentication
    ) {
        notificationService.markAllRead(
                authentication.getName()
        );

        return ApiResponse.success(
                Map.of(
                        "message",
                        "모든 알림을 읽음 처리했습니다."
                )
        );
    }
}

