import {
  apiClient,
  unwrapApiResponse,
} from '@/api/apiClient'
import type { ApiResponse } from '@/api/types'
import type { UserStatus } from '@/features/auth/types'
import type { FactCheckResponse } from '@/features/factcheck/factCheckTypes'
import type {
  AdminDashboard,
  AdminPost,
  AdminPostPage,
  AdminUser,
  AdminUserPage,
} from './adminTypes'

export async function getAdminDashboard(
  signal?: AbortSignal,
): Promise<AdminDashboard> {
  const response = await apiClient.get<
    ApiResponse<AdminDashboard>
  >('/admin/dashboard', { signal })
  return unwrapApiResponse(response.data, response.status)
}

export async function getAdminUsers(
  signal?: AbortSignal,
): Promise<AdminUserPage> {
  const response = await apiClient.get<
    ApiResponse<AdminUserPage>
  >('/admin/users', {
    params: { page: 0, size: 50 },
    signal,
  })
  return unwrapApiResponse(response.data, response.status)
}

export async function changeAdminUserStatus(
  userId: number,
  status: UserStatus,
): Promise<AdminUser> {
  const response = await apiClient.put<
    ApiResponse<AdminUser>
  >(`/admin/users/${userId}/status`, { status })
  return unwrapApiResponse(response.data, response.status)
}

export async function getAdminPosts(
  signal?: AbortSignal,
): Promise<AdminPostPage> {
  const response = await apiClient.get<
    ApiResponse<AdminPostPage>
  >('/admin/posts', {
    params: { page: 0, size: 50 },
    signal,
  })
  return unwrapApiResponse(response.data, response.status)
}

export async function changeAdminPostVisibility(
  postId: number,
  hidden: boolean,
): Promise<AdminPost> {
  const response = await apiClient.put<
    ApiResponse<AdminPost>
  >(`/admin/posts/${postId}/visibility`, { hidden })
  return unwrapApiResponse(response.data, response.status)
}

export async function forceAdminAnalysis(
  postId: number,
): Promise<FactCheckResponse> {
  const response = await apiClient.post<
    ApiResponse<FactCheckResponse>
  >(`/admin/posts/${postId}/analyses`)
  return unwrapApiResponse(response.data, response.status)
}

