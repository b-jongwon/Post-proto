import type {
  PageResponse,
} from '@/features/posts/postTypes'
import type {
  UserRole,
  UserStatus,
} from '@/features/auth/types'

export interface AdminDashboard {
  totalUserCount: number
  activeUserCount: number
  suspendedUserCount: number
  publishedPostCount: number
  hiddenPostCount: number
  totalLikeCount: number
  totalCommentCount: number
  todayPostCount: number
  todayCommentCount: number
}

export interface AdminUser {
  userId: number
  email: string
  nickname: string
  fullName: string | null
  birthYear: number | null
  age: number | null
  role: UserRole
  status: UserStatus
  emailVerifiedAt: string
  createdAt: string
}

export interface AdminPost {
  postId: number
  title: string
  category: string
  authorId: number
  authorNickname: string
  status: 'PUBLISHED' | 'HIDDEN'
  viewCount: number
  likeCount: number
  commentCount: number
  createdAt: string
}

export type AdminUserPage = PageResponse<AdminUser>
export type AdminPostPage = PageResponse<AdminPost>

