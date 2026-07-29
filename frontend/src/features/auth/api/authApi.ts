import { ApiError } from '@/api/ApiError'
import { apiClient, unwrapApiResponse } from '@/api/apiClient'
import { clearCsrfToken } from '@/api/csrf'
import type { ApiResponse } from '@/api/types'
import type {
  LoginRequest,
  LoginResponse,
  LogoutResponse,
  MyInfoResponse,
  SignupRequest,
  SignupResponse,
} from '@/features/auth/types'

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const response = await apiClient.post<ApiResponse<LoginResponse>>('/auth/login', payload)
  clearCsrfToken()
  return unwrapApiResponse(response.data, response.status)
}

export async function signup(payload: SignupRequest): Promise<SignupResponse> {
  const response = await apiClient.post<ApiResponse<SignupResponse>>('/auth/signup', payload)
  return unwrapApiResponse(response.data, response.status)
}

export async function getCurrentUser(): Promise<MyInfoResponse | null> {
  try {
    const response = await apiClient.get<ApiResponse<MyInfoResponse>>('/auth/me')
    return unwrapApiResponse(response.data, response.status)
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      return null
    }

    throw error
  }
}

export async function logout(): Promise<LogoutResponse> {
  const response = await apiClient.post<ApiResponse<LogoutResponse>>('/auth/logout')
  clearCsrfToken()
  return unwrapApiResponse(response.data, response.status)
}
