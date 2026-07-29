import axios from 'axios'
import { ApiError, toApiError } from '@/api/ApiError'
import type { ApiResponse, CsrfTokenResponse } from '@/api/types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const csrfClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    Accept: 'application/json',
  },
})

let cachedToken: CsrfTokenResponse | null = null
let pendingRequest: Promise<CsrfTokenResponse> | null = null

async function requestCsrfToken(): Promise<CsrfTokenResponse> {
  try {
    const response = await csrfClient.get<ApiResponse<CsrfTokenResponse>>('/csrf')

    if (!response.data.success || !response.data.data) {
      throw new ApiError({
        message: response.data.error?.message ?? 'CSRF 토큰을 발급받지 못했습니다.',
        status: response.status,
        code: response.data.error?.code ?? 'CSRF_TOKEN_ERROR',
        fields: response.data.error?.fields,
      })
    }

    cachedToken = response.data.data
    return cachedToken
  } catch (error) {
    throw toApiError(error)
  } finally {
    pendingRequest = null
  }
}

export function getCsrfToken(forceRefresh = false): Promise<CsrfTokenResponse> {
  if (forceRefresh) {
    cachedToken = null
  }

  if (cachedToken) {
    return Promise.resolve(cachedToken)
  }

  pendingRequest ??= requestCsrfToken()
  return pendingRequest
}

export function clearCsrfToken(): void {
  cachedToken = null
  pendingRequest = null
}
