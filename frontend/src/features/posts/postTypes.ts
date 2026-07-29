import type {
    FactCheckStatus,
    FactCheckVerdict,
} from '@/features/factcheck/factCheckTypes'

export interface ApiErrorBody {
    code?: string
    message?: string
    fields?: Record<string, string>
}

export interface ApiResponse<T> {
    success: boolean
    data: T | null
    error: ApiErrorBody | null
}

export interface PageResponse<T> {
    content: T[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

export interface PostSummary {
    postId: number
    title: string
    category: string
    authorId: number
    authorNickname: string
    viewCount: number
    createdAt: string

    analysisId: number | null
    analysisStatus: FactCheckStatus | null
    analysisVerdict: FactCheckVerdict | null
    credibilityScore: number | null
    analysisSummary: string | null
    analysisCompletedAt: string | null
    analysisStale: boolean

    content?: string
    contentPreview?: string
}

export interface PostStatistics {
    totalPostCount: number
    completedVerificationCount: number
    pendingVerificationCount: number
}

export interface PostDetail {
    postId: number
    title: string
    content: string
    category: string
    authorId: number
    authorNickname: string
    status: string
    viewCount: number
    createdAt: string
    updatedAt: string
}

export interface PostCreateRequest {
    title: string
    content: string
    category: string
}

export interface PostUpdateRequest {
    title: string
    content: string
    category: string
}

export interface PostDeleteResponse {
    message: string
}

export type PostSort = 'latest' | 'views'

export interface GetPostsParams {
    page: number
    size: number
    keyword?: string
    category?: string
    sort: PostSort
}