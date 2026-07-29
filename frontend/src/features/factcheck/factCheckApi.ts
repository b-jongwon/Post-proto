import { ApiError } from '@/api/ApiError'
import {
    apiClient,
    unwrapApiResponse,
} from '@/api/apiClient'
import type { ApiResponse } from '@/api/types'
import type {
    FactCheckHistoryItem,
    FactCheckResponse,
} from './factCheckTypes'

export async function getFactCheckAnalysis(
    postId: number,
    signal?: AbortSignal,
): Promise<FactCheckResponse | null> {
    try {
        const response = await apiClient.get<
            ApiResponse<FactCheckResponse>
        >(`/posts/${postId}/analysis`, {
            signal,
        })

        return unwrapApiResponse(
            response.data,
            response.status,
        )
    } catch (error) {
        if (
            error instanceof ApiError
            && error.status === 404
        ) {
            return null
        }

        throw error
    }
}

export async function runFactCheckAnalysis(
    postId: number,
): Promise<FactCheckResponse> {
    const response = await apiClient.post<
        ApiResponse<FactCheckResponse>
    >(`/posts/${postId}/analyses`)

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function getFactCheckHistory(
    postId: number,
    signal?: AbortSignal,
): Promise<FactCheckHistoryItem[]> {
    const response = await apiClient.get<
        ApiResponse<FactCheckHistoryItem[]>
    >(`/posts/${postId}/analyses`, {
        signal,
    })

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function getFactCheckDetail(
    postId: number,
    analysisId: number,
    signal?: AbortSignal,
): Promise<FactCheckResponse> {
    const response = await apiClient.get<
        ApiResponse<FactCheckResponse>
    >(`/posts/${postId}/analyses/${analysisId}`, {
        signal,
    })

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function changeRepresentativeAnalysis(
    postId: number,
    analysisId: number,
): Promise<FactCheckResponse> {
    const response = await apiClient.put<
        ApiResponse<FactCheckResponse>
    >(`/posts/${postId}/analysis-selection`, {
        analysisId,
    })

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export function getFactCheckErrorMessage(
    error: unknown,
): string {
    if (error instanceof ApiError) {
        if (error.status === 401) {
            return '로그인이 필요합니다.'
        }

        if (error.status === 403) {
            return '게시글 작성자 또는 관리자만 이 작업을 수행할 수 있습니다.'
        }

        if (error.status === 404) {
            return error.message
        }

        if (error.status === 405) {
            return '팩트체크 API 요청 방식이 올바르지 않습니다.'
        }

        if (error.status === 409) {
            return error.message
        }

        if (error.status === 429) {
            return 'AI 요청이 많습니다. 잠시 후 다시 시도해 주세요.'
        }

        if (error.code === 'GEMINI_NOT_CONFIGURED') {
            return 'Gemini API 키가 설정되지 않았습니다.'
        }

        if (
            error.code === 'GEMINI_API_ERROR'
            || error.code === 'INVALID_GEMINI_RESPONSE'
        ) {
            return error.message
        }

        if (error.code === 'NETWORK_ERROR') {
            return '백엔드 서버에 연결할 수 없습니다.'
        }

        return error.message
    }

    if (error instanceof Error) {
        return error.message
    }

    return '팩트체크 요청 중 알 수 없는 오류가 발생했습니다.'
}