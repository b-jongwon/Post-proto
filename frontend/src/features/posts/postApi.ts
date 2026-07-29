import { ApiError } from '@/api/ApiError'
import {
    apiClient,
    unwrapApiResponse,
} from '@/api/apiClient'
import type { ApiResponse } from '@/api/types'
import type {
    GetPostsParams,
    PageResponse,
    PostCreateRequest,
    PostDeleteResponse,
    PostDetail,
    PostStatistics,
    PostSummary,
    PostUpdateRequest,
} from './postTypes'

export async function getPosts(
    params: GetPostsParams,
    signal?: AbortSignal,
): Promise<PageResponse<PostSummary>> {
    const response = await apiClient.get<
        ApiResponse<PageResponse<PostSummary>>
    >('/posts', {
        params,
        signal,
    })

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function getPostStatistics(
    signal?: AbortSignal,
): Promise<PostStatistics> {
    const response = await apiClient.get<
        ApiResponse<PostStatistics>
    >('/posts/statistics', {
        signal,
    })

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function getPost(
    postId: number,
    signal?: AbortSignal,
): Promise<PostDetail> {
    const response = await apiClient.get<
        ApiResponse<PostDetail>
    >(`/posts/${postId}`, {
        signal,
    })

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function createPost(
    request: PostCreateRequest,
): Promise<PostDetail> {
    const response = await apiClient.post<
        ApiResponse<PostDetail>
    >('/posts', request)

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function updatePost(
    postId: number,
    request: PostUpdateRequest,
): Promise<PostDetail> {
    const response = await apiClient.put<
        ApiResponse<PostDetail>
    >(`/posts/${postId}`, request)

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function deletePost(
    postId: number,
): Promise<PostDeleteResponse> {
    const response = await apiClient.delete<
        ApiResponse<PostDeleteResponse>
    >(`/posts/${postId}`)

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export function getPostErrorMessage(
    error: unknown,
): string {
    if (error instanceof ApiError) {
        if (error.status === 401) {
            return '로그인이 필요합니다. 다시 로그인해 주세요.'
        }

        if (error.status === 403) {
            return '이 게시글을 수정하거나 삭제할 권한이 없습니다.'
        }

        if (error.status === 404) {
            return '존재하지 않거나 삭제된 게시글입니다.'
        }

        if (error.code === 'NETWORK_ERROR') {
            return '백엔드 서버에 연결할 수 없습니다. Spring Boot 실행 상태를 확인해 주세요.'
        }

        return error.message
    }

    if (error instanceof Error) {
        return error.message
    }

    return '게시글 요청 중 알 수 없는 오류가 발생했습니다.'
}