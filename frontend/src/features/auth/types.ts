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
  role: UserRole
  status: UserStatus
  createdAt: string
}

export interface MyInfoResponse {
  userId: number
  email: string
  nickname: string
  role: UserRole
  status: UserStatus
  createdAt: string
}

export interface LogoutResponse {
  message: string
}
