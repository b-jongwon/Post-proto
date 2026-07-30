import { useQuery } from '@tanstack/react-query'
import { Bell } from 'lucide-react'
import { NavLink } from 'react-router-dom'
import { getUnreadNotificationCount } from '@/features/notifications/notificationApi'

export function NotificationBell({
  enabled,
}: {
  enabled: boolean
}) {
  const unreadQuery = useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: ({ signal }) =>
      getUnreadNotificationCount(signal),
    enabled,
    staleTime: 15_000,
    refetchInterval: enabled ? 30_000 : false,
    retry: false,
  })

  const unreadCount =
    unreadQuery.data?.unreadCount ?? 0

  if (!enabled) {
    return null
  }

  return (
    <NavLink
      to="/notifications"
      className="icon-button desktop-only notification-bell"
      aria-label={
        unreadCount > 0
          ? `읽지 않은 알림 ${unreadCount}개`
          : '알림'
      }
    >
      <Bell size={20} />
      {unreadCount > 0 && (
        <span>
          {unreadCount > 99 ? '99+' : unreadCount}
        </span>
      )}
    </NavLink>
  )
}

