import axios, {
  type AxiosError,
  type InternalAxiosRequestConfig,
} from 'axios'
import { ApiError, toApiError } from '@/api/ApiError'
import { clearCsrfToken, getCsrfToken } from '@/api/csrf'
import type { ApiResponse } from '@/api/types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'
const MUTATING_METHODS = new Set(['post', 'put', 'patch', 'delete'])
const CSRF_EXEMPT_PATHS = ['/auth/login', '/auth/signup']

type RetryableConfig = InternalAxiosRequestConfig & {
  __csrfRetried?: boolean
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

function needsCsrf(config: InternalAxiosRequestConfig): boolean {
  const method = config.method?.toLowerCase()
  const url = config.url ?? ''

  return Boolean(
    method
      && MUTATING_METHODS.has(method)
      && !CSRF_EXEMPT_PATHS.some((path) => url.endsWith(path)),
  )
}

apiClient.interceptors.request.use(async (config) => {
  if (needsCsrf(config)) {
    const csrf = await getCsrfToken()
    config.headers.set(csrf.headerName, csrf.token)
  }

  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const config = error.config as RetryableConfig | undefined

    if (error.response?.status === 403 && config && needsCsrf(config) && !config.__csrfRetried) {
      config.__csrfRetried = true
      clearCsrfToken()
      const csrf = await getCsrfToken(true)
      config.headers.set(csrf.headerName, csrf.token)
      return apiClient.request(config)
    }

    return Promise.reject(toApiError(error))
  },
)

export function unwrapApiResponse<T>(response: ApiResponse<T>, status?: number): T {
  if (!response.success || response.data === null) {
    throw new ApiError({
      message: response.error?.message ?? '요청을 처리하지 못했습니다.',
      status: status ?? null,
      code: response.error?.code ?? 'INVALID_API_RESPONSE',
      fields: response.error?.fields,
    })
  }

  return response.data
}
