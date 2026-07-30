import { zodResolver } from '@hookform/resolvers/zod'
import {
  ArrowLeft,
  BadgeCheck,
  CakeSlice,
  Check,
  Eye,
  EyeOff,
  LoaderCircle,
  LockKeyhole,
  Mail,
  Send,
  UserRound,
} from 'lucide-react'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import {
  Link,
  Navigate,
  useNavigate,
} from 'react-router-dom'
import { ApiError } from '@/api/ApiError'
import {
  confirmEmailVerification,
  requestEmailVerification,
} from '@/features/auth/api/authApi'
import {
  useSession,
  useSignup,
} from '@/features/auth/hooks/useAuth'
import {
  signupSchema,
  type SignupFormValues,
} from '@/features/auth/schemas/authSchemas'

const currentYear = new Date().getFullYear()

export function SignupPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [serverMessage, setServerMessage] =
    useState<string | null>(null)
  const [verificationCode, setVerificationCode] =
    useState('')
  const [verificationMessage, setVerificationMessage] =
    useState<string | null>(null)
  const [verifiedEmail, setVerifiedEmail] =
    useState<string | null>(null)
  const [isRequestingCode, setIsRequestingCode] =
    useState(false)
  const [isConfirmingCode, setIsConfirmingCode] =
    useState(false)

  const navigate = useNavigate()
  const session = useSession()
  const signupMutation = useSignup()

  const birthYears = useMemo(
    () =>
      Array.from(
        { length: currentYear - 1899 },
        (_, index) => currentYear - index,
      ),
    [],
  )

  const {
    register,
    handleSubmit,
    getValues,
    setValue,
    setError,
    trigger,
    formState: { errors },
  } = useForm<SignupFormValues>({
    resolver: zodResolver(signupSchema),
    defaultValues: {
      fullName: '',
      birthYear: 2000,
      nickname: '',
      email: '',
      password: '',
      confirmPassword: '',
      emailVerificationToken: '',
      termsAccepted: false,
    },
  })

  if (session.data) {
    return <Navigate to="/" replace />
  }

  const invalidateEmailVerification = () => {
    if (verifiedEmail !== null) {
      setVerifiedEmail(null)
      setVerificationMessage(
        '이메일이 변경되어 다시 인증해야 합니다.',
      )
      setValue('emailVerificationToken', '')
    }
  }

  const handleRequestCode = async () => {
    const emailIsValid = await trigger('email')

    if (!emailIsValid) {
      return
    }

    const email = getValues('email').trim().toLowerCase()
    setServerMessage(null)
    setVerificationMessage(null)
    setIsRequestingCode(true)

    try {
      const response =
        await requestEmailVerification(email)

      setVerifiedEmail(null)
      setValue('emailVerificationToken', '')

      if (response.developmentCode) {
        setVerificationCode(
          response.developmentCode,
        )
        setVerificationMessage(
          `로컬 개발 인증 코드 ${response.developmentCode}가 준비되었습니다.`,
        )
      } else {
        setVerificationMessage(
          '인증 메일을 보냈습니다. 10분 안에 코드를 입력해주세요.',
        )
      }
    } catch (error) {
      setVerificationMessage(
        error instanceof ApiError
          ? error.message
          : '인증 메일을 요청하지 못했습니다.',
      )
    } finally {
      setIsRequestingCode(false)
    }
  }

  const handleConfirmCode = async () => {
    const emailIsValid = await trigger('email')

    if (!emailIsValid) {
      return
    }

    if (!/^\d{6}$/.test(verificationCode)) {
      setVerificationMessage(
        '숫자 6자리 인증 코드를 입력해주세요.',
      )
      return
    }

    const email = getValues('email').trim().toLowerCase()
    setIsConfirmingCode(true)
    setVerificationMessage(null)

    try {
      const response =
        await confirmEmailVerification(
          email,
          verificationCode,
        )

      setValue(
        'emailVerificationToken',
        response.verificationToken,
        { shouldValidate: true },
      )
      setVerifiedEmail(email)
      setVerificationMessage(
        '이메일 인증이 완료되었습니다.',
      )
    } catch (error) {
      setVerificationMessage(
        error instanceof ApiError
          ? error.message
          : '인증 코드를 확인하지 못했습니다.',
      )
    } finally {
      setIsConfirmingCode(false)
    }
  }

  const onSubmit = handleSubmit(async (values) => {
    setServerMessage(null)

    const normalizedEmail =
      values.email.trim().toLowerCase()

    if (
      !values.emailVerificationToken
      || verifiedEmail !== normalizedEmail
    ) {
      setError('emailVerificationToken', {
        message: '현재 이메일의 인증을 완료해주세요.',
      })
      return
    }

    try {
      await signupMutation.mutateAsync({
        fullName: values.fullName.trim(),
        birthYear: values.birthYear,
        nickname: values.nickname.trim(),
        email: normalizedEmail,
        password: values.password,
        emailVerificationToken:
          values.emailVerificationToken,
      })

      navigate('/login', {
        replace: true,
        state: {
          message:
            '이메일 인증과 회원가입이 완료되었습니다. 새 계정으로 로그인해주세요.',
        },
      })
    } catch (error) {
      if (error instanceof ApiError) {
        Object.entries(error.fields).forEach(
          ([field, message]) => {
            if (
              field === 'fullName'
              || field === 'birthYear'
              || field === 'nickname'
              || field === 'email'
              || field === 'password'
              || field === 'emailVerificationToken'
            ) {
              setError(field, { message })
            }
          },
        )
        setServerMessage(error.message)
        return
      }

      setServerMessage(
        '회원가입 중 오류가 발생했습니다.',
      )
    }
  })

  const emailRegistration = register('email')

  return (
    <section className="simple-auth-page">
      <div className="simple-auth-page__card signup-card">
        <Link to="/" className="back-link">
          <ArrowLeft size={17} /> 홈으로
        </Link>

        <div className="auth-form-card__heading">
          <span className="auth-step">
            01 · 본인 확인과 계정 만들기
          </span>
          <h1>FactHub를 시작해보세요</h1>
          <p>
            신뢰할 수 있는 커뮤니티를 위해 이메일
            인증을 완료해주세요.
          </p>
        </div>

        {serverMessage && (
          <div
            className="form-alert form-alert--error"
            role="alert"
          >
            {serverMessage}
          </div>
        )}

        <form
          className="auth-form"
          onSubmit={onSubmit}
          noValidate
        >
          <div className="signup-field-grid">
            <label>
              <span>이름</span>
              <div
                className={`input-shell${
                  errors.fullName ? ' is-error' : ''
                }`}
              >
                <UserRound size={18} />
                <input
                  type="text"
                  placeholder="실명"
                  autoComplete="name"
                  {...register('fullName')}
                />
              </div>
              {errors.fullName && (
                <small className="field-error">
                  {errors.fullName.message}
                </small>
              )}
            </label>

            <label>
              <span>출생연도 · 나이</span>
              <div
                className={`input-shell${
                  errors.birthYear ? ' is-error' : ''
                }`}
              >
                <CakeSlice size={18} />
                <select
                  aria-label="출생연도"
                  {...register('birthYear', {
                    valueAsNumber: true,
                  })}
                >
                  {birthYears.map((year) => (
                    <option key={year} value={year}>
                      {year}년 · {currentYear - year}세
                    </option>
                  ))}
                </select>
              </div>
              {errors.birthYear && (
                <small className="field-error">
                  {errors.birthYear.message}
                </small>
              )}
            </label>
          </div>

          <label>
            <span>닉네임</span>
            <div
              className={`input-shell${
                errors.nickname ? ' is-error' : ''
              }`}
            >
              <UserRound size={18} />
              <input
                type="text"
                placeholder="2~20자 닉네임"
                autoComplete="nickname"
                {...register('nickname')}
              />
            </div>
            {errors.nickname && (
              <small className="field-error">
                {errors.nickname.message}
              </small>
            )}
          </label>

          <fieldset className="email-verification-box">
            <legend>이메일 인증</legend>

            <label>
              <span>로그인 이메일</span>
              <div
                className={`input-shell${
                  errors.email ? ' is-error' : ''
                }`}
              >
                <Mail size={18} />
                <input
                  type="email"
                  placeholder="name@example.com"
                  autoComplete="email"
                  {...emailRegistration}
                  onChange={(event) => {
                    void emailRegistration.onChange(event)
                    invalidateEmailVerification()
                  }}
                />
                {verifiedEmail ? (
                  <BadgeCheck
                    className="verification-success-icon"
                    size={19}
                  />
                ) : (
                  <button
                    type="button"
                    className="input-inline-action"
                    onClick={() =>
                      void handleRequestCode()
                    }
                    disabled={isRequestingCode}
                  >
                    {isRequestingCode ? (
                      <LoaderCircle
                        className="spin"
                        size={16}
                      />
                    ) : (
                      <Send size={16} />
                    )}
                    인증 요청
                  </button>
                )}
              </div>
              {errors.email && (
                <small className="field-error">
                  {errors.email.message}
                </small>
              )}
            </label>

            {!verifiedEmail && (
              <label>
                <span>인증 코드</span>
                <div className="verification-code-row">
                  <div className="input-shell">
                    <Check size={18} />
                    <input
                      type="text"
                      inputMode="numeric"
                      maxLength={6}
                      placeholder="숫자 6자리"
                      value={verificationCode}
                      onChange={(event) =>
                        setVerificationCode(
                          event.target.value
                            .replace(/\D/g, '')
                            .slice(0, 6),
                        )
                      }
                    />
                  </div>
                  <button
                    type="button"
                    className="button button--secondary button--md"
                    onClick={() =>
                      void handleConfirmCode()
                    }
                    disabled={isConfirmingCode}
                  >
                    {isConfirmingCode ? (
                      <LoaderCircle
                        className="spin"
                        size={17}
                      />
                    ) : (
                      <BadgeCheck size={17} />
                    )}
                    확인
                  </button>
                </div>
              </label>
            )}

            {verificationMessage && (
              <p
                className={
                  verifiedEmail
                    ? 'verification-message verification-message--success'
                    : 'verification-message'
                }
              >
                {verificationMessage}
              </p>
            )}

            <input
              type="hidden"
              {...register('emailVerificationToken')}
            />
            {errors.emailVerificationToken && (
              <small className="field-error">
                {errors.emailVerificationToken.message}
              </small>
            )}
          </fieldset>

          <label>
            <span>비밀번호</span>
            <div
              className={`input-shell${
                errors.password ? ' is-error' : ''
              }`}
            >
              <LockKeyhole size={18} />
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder="8~64자 비밀번호"
                autoComplete="new-password"
                {...register('password')}
              />
              <button
                type="button"
                aria-label={
                  showPassword
                    ? '비밀번호 숨기기'
                    : '비밀번호 보기'
                }
                onClick={() =>
                  setShowPassword((value) => !value)
                }
              >
                {showPassword ? (
                  <EyeOff size={18} />
                ) : (
                  <Eye size={18} />
                )}
              </button>
            </div>
            {errors.password && (
              <small className="field-error">
                {errors.password.message}
              </small>
            )}
          </label>

          <label>
            <span>비밀번호 확인</span>
            <div
              className={`input-shell${
                errors.confirmPassword
                  ? ' is-error'
                  : ''
              }`}
            >
              <Check size={18} />
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder="비밀번호를 한 번 더 입력하세요"
                autoComplete="new-password"
                {...register('confirmPassword')}
              />
            </div>
            {errors.confirmPassword && (
              <small className="field-error">
                {errors.confirmPassword.message}
              </small>
            )}
          </label>

          <div className="password-guide">
            <span><Check size={14} /> 8~64자</span>
            <span>
              <Check size={14} /> 안전하게 암호화해 저장
            </span>
          </div>

          <label className="terms-checkbox">
            <input
              type="checkbox"
              {...register('termsAccepted')}
            />
            <span>
              서비스 이용약관과 개인정보 처리방침에
              동의합니다.
            </span>
          </label>
          {errors.termsAccepted && (
            <small className="field-error">
              {errors.termsAccepted.message}
            </small>
          )}

          <button
            type="submit"
            className="button button--primary button--lg button--full"
            disabled={signupMutation.isPending}
          >
            {signupMutation.isPending ? (
              <>
                <LoaderCircle
                  className="spin"
                  size={18}
                />
                계정 만드는 중
              </>
            ) : (
              '인증된 계정 만들기'
            )}
          </button>
        </form>

        <p className="auth-form-card__footer">
          이미 계정이 있나요?{' '}
          <Link to="/login">로그인</Link>
        </p>
      </div>
    </section>
  )
}
