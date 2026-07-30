import {
  apiClient,
  unwrapApiResponse,
} from '@/api/apiClient'
import type { ApiResponse } from '@/api/types'
import type { MyDashboard } from './profileTypes'

export async function getMyDashboard(
  signal?: AbortSignal,
): Promise<MyDashboard> {
  const response = await apiClient.get<
    ApiResponse<MyDashboard>
  >('/me/dashboard', { signal })

  return unwrapApiResponse(
    response.data,
    response.status,
  )
}

