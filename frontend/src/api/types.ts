export interface ApiErrorDetail {
  code: string
  message: string
  fields: Record<string, string>
}

export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: ApiErrorDetail | null
}

export interface CsrfTokenResponse {
  headerName: string
  parameterName: string
  token: string
}
