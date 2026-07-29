import { zodResolver } from '@hookform/resolvers/zod'
import {
  ArrowLeft,
  Check,
  Eye,
  EyeOff,
  LoaderCircle,
  LockKeyhole,
  Mail,
  UserRound,
} from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { ApiError } from '@/api/ApiError'
import { useSession, useSignup } from '@/features/auth/hooks/useAuth'
import {
  signupSchema,
  type SignupFormValues,
} from '@/features/auth/schemas/authSchemas'

export function SignupPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [serverMessage, setServerMessage] = useState<string | null>(null)
  const navigate = useNavigate()
  const session = useSession()
  const signupMutation = useSignup()

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<SignupFormValues>({
    resolver: zodResolver(signupSchema),
    defaultValues: {
      nickname: '',
      email: '',
      password: '',
      confirmPassword: '',
      termsAccepted: false,
    },
  })

  if (session.data) {
    return <Navigate to="/" replace />
  }

  const onSubmit = handleSubmit(async (values) => {
    setServerMessage(null)

    try {
      await signupMutation.mutateAsync({
        nickname: values.nickname.trim(),
        email: values.email.trim(),
        password: values.password,
      })

      navigate('/login', {
        replace: true,
        state: { message: '회원가입이 완료되었습니다. 새 계정으로 로그인해주세요.' },
      })
    } catch (error) {
      if (error instanceof ApiError) {
        Object.entries(error.fields).forEach(([field, message]) => {
          if (field === 'nickname' || field === 'email' || field === 'password') {
            setError(field, { message })
          }
        })
        setServerMessage(error.message)
        return
      }

      setServerMessage('회원가입 중 오류가 발생했습니다.')
    }
  })

  return (
    <section className="simple-auth-page">
      <div className="simple-auth-page__card">
        <Link to="/" className="back-link"><ArrowLeft size={17} /> 홈으로</Link>
        <div className="auth-form-card__heading">
          <span className="auth-step">01 · 계정 만들기</span>
          <h1>FactHub를 시작해보세요</h1>
          <p>1분이면 가입하고 팩트체크 커뮤니티에 참여할 수 있어요.</p>
        </div>

        {serverMessage && (
          <div className="form-alert form-alert--error" role="alert">
            {serverMessage}
          </div>
        )}

        <form className="auth-form" onSubmit={onSubmit} noValidate>
          <label>
            <span>닉네임</span>
            <div className={`input-shell${errors.nickname ? ' is-error' : ''}`}>
              <UserRound size={18} />
              <input
                type="text"
                placeholder="2~20자 닉네임"
                autoComplete="nickname"
                aria-invalid={Boolean(errors.nickname)}
                {...register('nickname')}
              />
            </div>
            {errors.nickname && <small className="field-error">{errors.nickname.message}</small>}
          </label>

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
                placeholder="8~64자 비밀번호"
                autoComplete="new-password"
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

          <label>
            <span>비밀번호 확인</span>
            <div className={`input-shell${errors.confirmPassword ? ' is-error' : ''}`}>
              <Check size={18} />
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder="비밀번호를 한 번 더 입력하세요"
                autoComplete="new-password"
                aria-invalid={Boolean(errors.confirmPassword)}
                {...register('confirmPassword')}
              />
            </div>
            {errors.confirmPassword && <small className="field-error">{errors.confirmPassword.message}</small>}
          </label>

          <div className="password-guide">
            <span><Check size={14} /> 8~64자</span>
            <span><Check size={14} /> 안전하게 암호화해 저장</span>
          </div>

          <label className="terms-checkbox">
            <input type="checkbox" {...register('termsAccepted')} />
            <span>서비스 이용약관과 개인정보 처리방침에 동의합니다.</span>
          </label>
          {errors.termsAccepted && <small className="field-error">{errors.termsAccepted.message}</small>}

          <button
            type="submit"
            className="button button--primary button--lg button--full"
            disabled={signupMutation.isPending}
          >
            {signupMutation.isPending ? (
              <><LoaderCircle className="spin" size={18} /> 계정 만드는 중</>
            ) : '가입하고 시작하기'}
          </button>
        </form>

        <p className="auth-form-card__footer">
          이미 계정이 있나요? <Link to="/login">로그인</Link>
        </p>
      </div>
    </section>
  )
}
