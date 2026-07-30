package com.facthub.notification.dto;

import com.facthub.notification.domain.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        String type,
        String message,
        boolean read,
        Long actorId,
        String actorNickname,
        Long postId,
        String postTitle,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {

    public static NotificationResponse from(
            Notification notification
    ) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getMessage(),
                notification.isRead(),
                notification.getActor().getId(),
                notification.getActor().getNickname(),
                notification.getPost().getId(),
                notification.getPost().getTitle(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}

