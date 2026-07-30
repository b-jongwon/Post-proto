import type { PageResponse } from '@/features/posts/postTypes'

export type NotificationType =
  | 'COMMENT_CREATED'
  | 'POST_LIKED'

export interface NotificationItem {
  notificationId: number
  type: NotificationType
  message: string
  read: boolean
  actorId: number
  actorNickname: string
  postId: number
  postTitle: string
  createdAt: string
  readAt: string | null
}

export type NotificationPage =
  PageResponse<NotificationItem>

export interface UnreadNotificationCount {
  unreadCount: number
}

