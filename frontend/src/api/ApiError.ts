import axios from 'axios'
import type { ApiResponse } from '@/api/types'

export class ApiError extends Error {
  readonly status: number | null
  readonly code: string
  readonly fields: Record<string, string>

  constructor(options: {
    message: string
    status?: number | null
    code?: string
    fields?: Record<string, string>
  }) {
    super(options.message)
    this.name = 'ApiError'
    this.status = options.status ?? null
    this.code = options.code ?? 'UNKNOWN_ERROR'
    this.fields = options.fields ?? {}
  }
}

export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) {
    return error
  }

  if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
    const response = error.response
    const apiError = response?.data?.error

    if (apiError) {
      return new ApiError({
        message: apiError.message,
        status: response?.status ?? null,
        code: apiError.code,
        fields: apiError.fields,
      })
    }

    if (error.code === 'ERR_NETWORK') {
      return new ApiError({
        message: '서버에 연결할 수 없습니다. 백엔드 실행 상태를 확인해주세요.',
        code: 'NETWORK_ERROR',
      })
    }

    return new ApiError({
      message: error.message || '요청을 처리하지 못했습니다.',
      status: response?.status ?? null,
      code: 'HTTP_ERROR',
    })
  }

  if (error instanceof Error) {
    return new ApiError({ message: error.message })
  }

  return new ApiError({ message: '알 수 없는 오류가 발생했습니다.' })
}
