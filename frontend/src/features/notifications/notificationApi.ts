import {
  apiClient,
  unwrapApiResponse,
} from '@/api/apiClient'
import type { ApiResponse } from '@/api/types'
import type {
  NotificationItem,
  NotificationPage,
  UnreadNotificationCount,
} from './notificationTypes'

export async function getNotifications(
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<NotificationPage> {
  const response = await apiClient.get<
    ApiResponse<NotificationPage>
  >('/notifications', {
    params: { page, size },
    signal,
  })

  return unwrapApiResponse(
    response.data,
    response.status,
  )
}

export async function getUnreadNotificationCount(
  signal?: AbortSignal,
): Promise<UnreadNotificationCount> {
  const response = await apiClient.get<
    ApiResponse<UnreadNotificationCount>
  >('/notifications/unread-count', { signal })

  return unwrapApiResponse(
    response.data,
    response.status,
  )
}

export async function markNotificationRead(
  notificationId: number,
): Promise<NotificationItem> {
  const response = await apiClient.post<
    ApiResponse<NotificationItem>
  >(`/notifications/${notificationId}/read`)

  return unwrapApiResponse(
    response.data,
    response.status,
  )
}

export async function markAllNotificationsRead(): Promise<void> {
  const response = await apiClient.post<
    ApiResponse<{ message: string }>
  >('/notifications/read-all')

  unwrapApiResponse(
    response.data,
    response.status,
  )
}

