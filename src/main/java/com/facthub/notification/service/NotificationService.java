package com.facthub.notification.service;

import com.facthub.common.exception.BusinessException;
import com.facthub.common.response.PageResponse;
import com.facthub.notification.domain.Notification;
import com.facthub.notification.domain.NotificationType;
import com.facthub.notification.dto.NotificationResponse;
import com.facthub.notification.dto.UnreadNotificationCountResponse;
import com.facthub.notification.repository.NotificationRepository;
import com.facthub.post.domain.Post;
import com.facthub.user.domain.User;
import com.facthub.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationRepository
            notificationRepository;
    private final UserService userService;

    public NotificationService(
            NotificationRepository
                    notificationRepository,
            UserService userService
    ) {
        this.notificationRepository =
                notificationRepository;
        this.userService = userService;
    }

    @Transactional
    public void notifyComment(
            Post post,
            User actor
    ) {
        createNotification(
                post,
                actor,
                NotificationType.COMMENT_CREATED,
                "%s님이 내 글에 댓글을 남겼습니다."
                        .formatted(
                                actor.getNickname()
                        )
        );
    }

    @Transactional
    public void notifyLike(
            Post post,
            User actor
    ) {
        createNotification(
                post,
                actor,
                NotificationType.POST_LIKED,
                "%s님이 내 글을 좋아합니다."
                        .formatted(
                                actor.getNickname()
                        )
        );
    }

    public PageResponse<NotificationResponse>
    getNotifications(
            String userEmail,
            int page,
            int size
    ) {
        validatePagination(page, size);

        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        Page<NotificationResponse> responsePage =
                notificationRepository
                        .findByRecipient_IdOrderByCreatedAtDesc(
                                user.getId(),
                                PageRequest.of(
                                        page,
                                        size
                                )
                        )
                        .map(
                                NotificationResponse::from
                        );

        return PageResponse.from(responsePage);
    }

    public UnreadNotificationCountResponse
    getUnreadCount(String userEmail) {
        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        return new UnreadNotificationCountResponse(
                notificationRepository
                        .countByRecipient_IdAndReadFalse(
                                user.getId()
                        )
        );
    }

    @Transactional
    public NotificationResponse markRead(
            Long notificationId,
            String userEmail
    ) {
        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        Notification notification =
                notificationRepository
                        .findByIdAndRecipient_Id(
                                notificationId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "NOTIFICATION_NOT_FOUND",
                                        "알림을 찾을 수 없습니다.",
                                        HttpStatus.NOT_FOUND
                                )
                        );

        notification.markRead();

        return NotificationResponse.from(
                notification
        );
    }

    @Transactional
    public void markAllRead(String userEmail) {
        User user =
                userService.getActiveUserByEmail(
                        userEmail
                );

        notificationRepository
                .markAllReadByRecipientId(
                        user.getId(),
                        java.time.LocalDateTime.now()
                );
    }

    private void createNotification(
            Post post,
            User actor,
            NotificationType type,
            String message
    ) {
        User recipient = post.getAuthor();

        if (recipient.getId().equals(
                actor.getId()
        )) {
            return;
        }

        notificationRepository.save(
                Notification.create(
                        recipient,
                        actor,
                        post,
                        type,
                        message
                )
        );
    }

    private void validatePagination(
            int page,
            int size
    ) {
        if (page < 0
                || size < 1
                || size > MAX_PAGE_SIZE) {

            throw new IllegalArgumentException(
                    "알림 페이지 범위를 확인해주세요."
            );
        }
    }
}
