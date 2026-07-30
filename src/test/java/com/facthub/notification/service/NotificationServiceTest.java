package com.facthub.notification.service;

import com.facthub.notification.domain.Notification;
import com.facthub.notification.domain.NotificationType;
import com.facthub.notification.repository.NotificationRepository;
import com.facthub.post.domain.Post;
import com.facthub.user.domain.User;
import com.facthub.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private NotificationRepository
            notificationRepository;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        notificationRepository = Mockito.mock(
                NotificationRepository.class
        );
        service = new NotificationService(
                notificationRepository,
                Mockito.mock(UserService.class)
        );
    }

    @Test
    void notifyComment_createsNotificationForPostAuthor() {
        User recipient = Mockito.mock(User.class);
        User actor = Mockito.mock(User.class);
        Post post = Mockito.mock(Post.class);

        when(recipient.getId()).thenReturn(1L);
        when(actor.getId()).thenReturn(2L);
        when(actor.getNickname())
                .thenReturn("댓글작성자");
        when(post.getAuthor()).thenReturn(recipient);

        service.notifyComment(post, actor);

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(
                        Notification.class
                );

        verify(notificationRepository)
                .save(captor.capture());

        Notification notification =
                captor.getValue();

        assertThat(notification.getActor())
                .isSameAs(actor);
        assertThat(notification.getPost())
                .isSameAs(post);
        assertThat(notification.getType())
                .isEqualTo(
                        NotificationType.COMMENT_CREATED
                );
        assertThat(notification.getMessage())
                .contains("댓글작성자")
                .contains("댓글");
        assertThat(notification.isRead())
                .isFalse();
    }

    @Test
    void notifyLike_doesNotNotifyOwnPost() {
        User author = Mockito.mock(User.class);
        Post post = Mockito.mock(Post.class);

        when(author.getId()).thenReturn(1L);
        when(author.getNickname())
                .thenReturn("작성자");
        when(post.getAuthor()).thenReturn(author);

        service.notifyLike(post, author);

        verify(notificationRepository, never())
                .save(Mockito.any(Notification.class));
    }
}
