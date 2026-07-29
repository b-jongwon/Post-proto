import { ApiError } from '@/api/ApiError'
import {
    apiClient,
    unwrapApiResponse,
} from '@/api/apiClient'
import type { ApiResponse } from '@/api/types'
import type {
    CommentCreateRequest,
    CommentDeleteResponse,
    CommentItem,
    CommentPage,
    CommentUpdateRequest,
} from './commentTypes'

export async function getComments(
    postId: number,
    page: number,
    size: number,
    signal?: AbortSignal,
): Promise<CommentPage> {
    const response = await apiClient.get<
        ApiResponse<CommentPage>
    >(`/posts/${postId}/comments`, {
        params: {
            page,
            size,
        },
        signal,
    })

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function createComment(
    postId: number,
    request: CommentCreateRequest,
): Promise<CommentItem> {
    const response = await apiClient.post<
        ApiResponse<CommentItem>
    >(`/posts/${postId}/comments`, request)

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function updateComment(
    postId: number,
    commentId: number,
    request: CommentUpdateRequest,
): Promise<CommentItem> {
    const response = await apiClient.put<
        ApiResponse<CommentItem>
    >(
        `/posts/${postId}/comments/${commentId}`,
        request,
    )

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export async function deleteComment(
    postId: number,
    commentId: number,
): Promise<CommentDeleteResponse> {
    const response = await apiClient.delete<
        ApiResponse<CommentDeleteResponse>
    >(`/posts/${postId}/comments/${commentId}`)

    return unwrapApiResponse(
        response.data,
        response.status,
    )
}

export function getCommentErrorMessage(
    error: unknown,
): string {
    if (error instanceof ApiError) {
        if (error.status === 401) {
            return '댓글을 작성하려면 로그인이 필요합니다.'
        }

        if (error.status === 403) {
            return '이 댓글을 수정하거나 삭제할 권한이 없습니다.'
        }

        if (error.status === 404) {
            return '댓글 또는 게시글을 찾을 수 없습니다.'
        }

        if (error.code === 'VALIDATION_ERROR') {
            const firstFieldMessage = error.fields
                ? Object.values(error.fields)[0]
                : undefined

            return firstFieldMessage
                ?? '댓글 내용을 확인해 주세요.'
        }

        if (error.code === 'NETWORK_ERROR') {
            return '백엔드 서버에 연결할 수 없습니다.'
        }

        return error.message
    }

    if (error instanceof Error) {
        return error.message
    }

    return '댓글 요청 중 알 수 없는 오류가 발생했습니다.'
}