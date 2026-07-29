import type { PageResponse } from '@/features/posts/postTypes'

export type CommentStatus = 'ACTIVE' | 'DELETED'

export interface CommentItem {
    commentId: number
    postId: number
    authorId: number
    authorNickname: string
    content: string
    status: CommentStatus
    createdAt: string
    updatedAt: string
}

export interface CommentCreateRequest {
    content: string
}

export interface CommentUpdateRequest {
    content: string
}

export interface CommentDeleteResponse {
    message: string
}

export type CommentPage = PageResponse<CommentItem>