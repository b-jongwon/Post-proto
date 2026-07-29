import { zodResolver } from '@hookform/resolvers/zod'
import {
  ArrowLeft,
  Eye,
  EyeOff,
  LoaderCircle,
  LockKeyhole,
  Mail,
  ShieldCheck,
} from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '@/api/ApiError'
import { FactHubLogo } from '@/components/brand/FactHubLogo'
import { useLogin, useSession } from '@/features/auth/hooks/useAuth'
import {
  loginSchema,
  type LoginFormValues,
} from '@/features/auth/schemas/authSchemas'

interface LoginLocationState {
  from?: string
  message?: string
}

export function LoginPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [serverMessage, setServerMessage] = useState<string | null>(null)
  const navigate = useNavigate()
  const location = useLocation()
  const locationState = location.state as LoginLocationState | null
  const session = useSession()
  const loginMutation = useLogin()

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })

  if (session.data) {
    return <Navigate to="/" replace />
  }

  const onSubmit = handleSubmit(async (values) => {
    setServerMessage(null)

    try {
      await loginMutation.mutateAsync({
        email: values.email.trim(),
        password: values.password,
      })

      navigate(locationState?.from ?? '/', { replace: true })
    } catch (error) {
      if (error instanceof ApiError) {
        Object.entries(error.fields).forEach(([field, message]) => {
          if (field === 'email' || field === 'password') {
            setError(field, { message })
          }
        })
        setServerMessage(error.message)
        return
      }

      setServerMessage('로그인 중 오류가 발생했습니다.')
    }
  })

  return (
    <section className="auth-page">
      <div className="auth-page__aside">
        <div className="auth-page__aside-inner">
          <FactHubLogo />
          <div className="auth-page__aside-copy">
            <span className="auth-badge">FACTHUB VERIFIED COMMUNITY</span>
            <h1>확신보다 먼저,<br />근거를 확인하는 습관</h1>
            <p>FactHub에서 신뢰할 수 있는 정보와 검증 과정을 한눈에 확인하세요.</p>
          </div>
          <div className="auth-page__trust-card">
            <ShieldCheck size={22} />
            <div>
              <strong>출처 중심의 투명한 분석</strong>
              <span>모든 판정은 근거와 함께 제공됩니다.</span>
            </div>
          </div>
        </div>
      </div>

      <div className="auth-page__content">
        <div className="auth-form-card">
          <Link to="/" className="back-link"><ArrowLeft size={17} /> 홈으로</Link>
          <div className="auth-form-card__heading">
            <h2>다시 만나서 반가워요</h2>
            <p>FactHub 계정으로 로그인하세요.</p>
          </div>

          {locationState?.message && (
            <div className="form-alert form-alert--success" role="status">
              {locationState.message}
            </div>
          )}

          {serverMessage && (
            <div className="form-alert form-alert--error" role="alert">
              {serverMessage}
            </div>
          )}

          <form className="auth-form" onSubmit={onSubmit} noValidate>
            <label>
              <span>이메일</span>
              <div className={`input-shell${errors.email ? ' is-error' : ''}`}>
                <Mail size={18} />
                <input
                  type="email"
                  placeholder="name@example.com"
                  autoComplete="email"
                  aria-invalid={Boolean(errors.email)}
                  {...register('email')}
                />
              </div>
              {errors.email && <small className="field-error">{errors.email.message}</small>}
            </label>

            <label>
              <span>비밀번호</span>
              <div className={`input-shell${errors.password ? ' is-error' : ''}`}>
                <LockKeyhole size={18} />
                <input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="비밀번호를 입력하세요"
                  autoComplete="current-password"
                  aria-invalid={Boolean(errors.password)}
                  {...register('password')}
                />
                <button
                  type="button"
                  aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 보기'}
                  onClick={() => setShowPassword((value) => !value)}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {errors.password && <small className="field-error">{errors.password.message}</small>}
            </label>

            <button
              type="submit"
              className="button button--primary button--lg button--full"
              disabled={loginMutation.isPending}
            >
              {loginMutation.isPending ? (
                <><LoaderCircle className="spin" size={18} /> 로그인 중</>
              ) : '로그인'}
            </button>
          </form>

          <p className="auth-form-card__footer">
            아직 계정이 없나요? <Link to="/signup">회원가입</Link>
          </p>
        </div>
      </div>
    </section>
  )
}
