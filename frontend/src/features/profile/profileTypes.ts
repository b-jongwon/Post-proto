import type {
  MyCommentActivity,
  MyInfoResponse,
} from '@/features/auth/types'
import type { PostSummary } from '@/features/posts/postTypes'

export interface MyDashboard {
  profile: MyInfoResponse
  postCount: number
  commentCount: number
  likedPostCount: number
  unreadNotificationCount: number
  posts: PostSummary[]
  comments: MyCommentActivity[]
  likedPosts: PostSummary[]
}

