import { ApiError } from '@/api/ApiError'
import {
    apiClient,
    unwrapApiResponse,
} from '@/api/apiClient'
import type { ApiResponse } from '@/api/types'
import type { PostLikeResponse } from './postLikeTypes'

export async function getPostLikeStatus(
    postId: number,
    signal?: AbortSignal,
): Promise<PostLikeResponse> {
    const response = await apiClient.get<
        ApiResponse<PostLikeResponse>
    >(`/posts/${postId}/likes`, {
        signal,
    })

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function likePost(
    postId: number,
): Promise<PostLikeResponse> {
    const response = await apiClient.post<
        ApiResponse<PostLikeResponse>
    >(`/posts/${postId}/likes`)

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function unlikePost(
    postId: number,
): Promise<PostLikeResponse> {
    const response = await apiClient.delete<
        ApiResponse<PostLikeResponse>
    >(`/posts/${postId}/likes`)

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export function getPostLikeErrorMessage(
    error: unknown,
): string {
    if (error instanceof ApiError) {
        if (error.status === 401) {
            return '좋아요를 누르려면 로그인이 필요합니다.'
        }

        if (error.status === 404) {
            return '게시글을 찾을 수 없습니다.'
        }

        if (error.code === 'NETWORK_ERROR') {
            return '백엔드 서버에 연결할 수 없습니다.'
        }

        return error.message
    }

    if (error instanceof Error) {
        return error.message
    }

    return '좋아요 요청 중 알 수 없는 오류가 발생했습니다.'
}