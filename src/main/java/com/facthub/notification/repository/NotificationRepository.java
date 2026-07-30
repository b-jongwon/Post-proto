package com.facthub.notification.repository;

import com.facthub.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    @EntityGraph(
            attributePaths = {
                    "actor",
                    "post"
            }
    )
    Page<Notification>
    findByRecipient_IdOrderByCreatedAtDesc(
            Long recipientId,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "actor",
                    "post"
            }
    )
    Optional<Notification>
    findByIdAndRecipient_Id(
            Long notificationId,
            Long recipientId
    );

    long countByRecipient_IdAndReadFalse(
            Long recipientId
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Notification notification
            SET notification.read = true,
                notification.readAt = :readAt
            WHERE notification.recipient.id =
                  :recipientId
              AND notification.read = false
            """)
    int markAllReadByRecipientId(
            @Param("recipientId")
            Long recipientId,
            @Param("readAt")
            LocalDateTime readAt
    );
}
