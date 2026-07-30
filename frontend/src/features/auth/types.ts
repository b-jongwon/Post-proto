export type UserRole = 'USER' | 'ADMIN'
export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'DELETED'

export interface LoginRequest {
  email: string
  password: string
}

export interface SignupRequest {
  email: string
  password: string
  nickname: string
  fullName: string
  birthYear: number
  emailVerificationToken: string
}

export interface LoginResponse {
  userId: number
  email: string
  nickname: string
  role: UserRole
}

export interface SignupResponse {
  userId: number
  email: string
  nickname: string
  fullName: string
  birthYear: number
  age: number
  emailVerifiedAt: string
  role: UserRole
  status: UserStatus
  createdAt: string
}

export interface MyInfoResponse {
  userId: number
  email: string
  nickname: string
  fullName: string | null
  birthYear: number | null
  age: number | null
  emailVerified: boolean
  emailVerifiedAt: string
  role: UserRole
  status: UserStatus
  createdAt: string
}

export interface LogoutResponse {
  message: string
}

export interface EmailVerificationIssueResponse {
  email: string
  expiresAt: string
  retryAfterSeconds: number
  developmentCode: string | null
}

export interface EmailVerificationConfirmResponse {
  verificationToken: string
  expiresAt: string
}

export interface MyCommentActivity {
  commentId: number
  postId: number
  postTitle: string
  content: string
  createdAt: string
  updatedAt: string
}
