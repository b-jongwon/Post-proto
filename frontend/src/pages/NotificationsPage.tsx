import {
  Bell,
  CheckCheck,
  Heart,
  MessageCircle,
  RotateCcw,
} from 'lucide-react'
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import {
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from '@/features/notifications/notificationApi'
import type { NotificationItem } from '@/features/notifications/notificationTypes'

function formatRelativeTime(value: string): string {
  const timestamp = new Date(value).getTime()
  const difference = Date.now() - timestamp

  if (!Number.isFinite(timestamp) || difference < 60_000) {
    return '방금 전'
  }

  const minutes = Math.floor(difference / 60_000)
  if (minutes < 60) {
    return `${minutes}분 전`
  }

  const hours = Math.floor(minutes / 60)
  if (hours < 24) {
    return `${hours}시간 전`
  }

  return new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(value))
}

export function NotificationsPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const notificationsQuery = useQuery({
    queryKey: ['notifications', 'list'],
    queryFn: ({ signal }) =>
      getNotifications(0, 50, signal),
    retry: false,
  })

  const markReadMutation = useMutation({
    mutationFn: (notificationId: number) =>
      markNotificationRead(notificationId),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: ['notifications'],
      })
    },
  })

  const markAllMutation = useMutation({
    mutationFn: markAllNotificationsRead,
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: ['notifications'],
      })
      void queryClient.invalidateQueries({
        queryKey: ['me', 'dashboard'],
      })
    },
  })

  const openNotification = (
    notification: NotificationItem,
  ) => {
    if (!notification.read) {
      markReadMutation.mutate(
        notification.notificationId,
      )
    }
    navigate(`/posts/${notification.postId}`)
  }

  const notifications =
    notificationsQuery.data?.content ?? []
  const unreadCount = notifications.filter(
    (notification) => !notification.read,
  ).length

  return (
    <section className="notifications-page">
      <div className="container notifications-page__inner">
        <header className="page-hero-heading">
          <div>
            <span>NOTIFICATIONS</span>
            <h1>내 알림</h1>
            <p>
              내 글에 도착한 댓글과 좋아요를 확인합니다.
            </p>
          </div>
          <button
            type="button"
            className="button button--secondary button--md"
            onClick={() => markAllMutation.mutate()}
            disabled={
              unreadCount === 0
              || markAllMutation.isPending
            }
          >
            <CheckCheck size={17} />
            모두 읽음
          </button>
        </header>

        <div className="notifications-card">
          <div className="notifications-card__summary">
            <span className="notifications-card__icon">
              <Bell size={20} />
            </span>
            <div>
              <strong>
                읽지 않은 알림 {unreadCount}개
              </strong>
              <p>
                최근 알림부터 최대 50개를 보여줍니다.
              </p>
            </div>
          </div>

          {notificationsQuery.isPending && (
            <div className="notifications-state">
              알림을 불러오고 있습니다.
            </div>
          )}

          {notificationsQuery.isError && (
            <div className="notifications-state notifications-state--error">
              <p>알림을 불러오지 못했습니다.</p>
              <button
                type="button"
                onClick={() =>
                  void notificationsQuery.refetch()
                }
              >
                <RotateCcw size={15} />
                다시 시도
              </button>
            </div>
          )}

          {!notificationsQuery.isPending
            && !notificationsQuery.isError
            && notifications.length === 0 && (
            <div className="notifications-state">
              <Bell size={27} />
              <strong>아직 새 알림이 없습니다.</strong>
              <p>
                댓글이나 좋아요가 생기면 이곳에 표시됩니다.
              </p>
            </div>
          )}

          <div className="notification-list">
            {notifications.map((notification) => {
              const Icon =
                notification.type === 'COMMENT_CREATED'
                  ? MessageCircle
                  : Heart

              return (
                <button
                  key={notification.notificationId}
                  type="button"
                  className={`notification-item${
                    notification.read
                      ? ''
                      : ' notification-item--unread'
                  }`}
                  onClick={() =>
                    openNotification(notification)
                  }
                >
                  <span className="notification-item__icon">
                    <Icon size={19} />
                  </span>
                  <div>
                    <strong>{notification.message}</strong>
                    <p>{notification.postTitle}</p>
                  </div>
                  <time>
                    {formatRelativeTime(
                      notification.createdAt,
                    )}
                  </time>
                </button>
              )
            })}
          </div>
        </div>
      </div>
    </section>
  )
}

