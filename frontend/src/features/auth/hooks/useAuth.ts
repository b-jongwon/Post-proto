import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  getCurrentUser,
  login,
  logout,
  signup,
} from '@/features/auth/api/authApi'
import type { LoginRequest, SignupRequest } from '@/features/auth/types'

export const SESSION_QUERY_KEY = ['auth', 'session'] as const

export function useSession() {
  return useQuery({
    queryKey: SESSION_QUERY_KEY,
    queryFn: getCurrentUser,
    staleTime: 30_000,
    retry: false,
  })
}

export function useLogin() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: LoginRequest) => {
      await login(payload)
      const user = await getCurrentUser()

      if (!user) {
        throw new Error('로그인 세션을 확인하지 못했습니다.')
      }

      return user
    },
    onSuccess: (user) => {
      queryClient.setQueryData(SESSION_QUERY_KEY, user)
    },
  })
}

export function useSignup() {
  return useMutation({
    mutationFn: (payload: SignupRequest) => signup(payload),
  })
}

export function useLogout() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: logout,
    onSuccess: () => {
      queryClient.setQueryData(SESSION_QUERY_KEY, null)
    },
  })
}
