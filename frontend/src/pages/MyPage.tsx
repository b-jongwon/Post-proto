import {
  CalendarDays,
  CheckCircle2,
  LogOut,
  Mail,
  ShieldCheck,
  UserRound,
} from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useLogout, useSession } from '@/features/auth/hooks/useAuth'

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(new Date(value))
}

export function MyPage() {
  const navigate = useNavigate()
  const session = useSession()
  const logoutMutation = useLogout()
  const user = session.data

  if (!user) {
    return null
  }

  const handleLogout = async () => {
    await logoutMutation.mutateAsync()
    navigate('/', { replace: true })
  }

  return (
    <section className="profile-page">
      <div className="container profile-page__inner">
        <div className="profile-heading">
          <span className="auth-step">MY FACTHUB</span>
          <h1>내 정보</h1>
          <p>계정과 활동 정보를 안전하게 관리합니다.</p>
        </div>

        <div className="profile-grid">
          <article className="profile-card profile-card--identity">
            <div className="profile-avatar" aria-hidden="true">
              {user.nickname.slice(0, 1).toUpperCase()}
            </div>
            <div>
              <div className="profile-name-line">
                <h2>{user.nickname}</h2>
                <span className="status-pill"><CheckCircle2 size={14} /> 활성 계정</span>
              </div>
              <p>{user.email}</p>
            </div>
          </article>

          <article className="profile-card profile-card--details">
            <h2>계정 정보</h2>
            <dl className="profile-details">
              <div>
                <dt><Mail size={17} /> 이메일</dt>
                <dd>{user.email}</dd>
              </div>
              <div>
                <dt><UserRound size={17} /> 닉네임</dt>
                <dd>{user.nickname}</dd>
              </div>
              <div>
                <dt><ShieldCheck size={17} /> 권한</dt>
                <dd>{user.role === 'ADMIN' ? '관리자' : '일반 사용자'}</dd>
              </div>
              <div>
                <dt><CalendarDays size={17} /> 가입일</dt>
                <dd>{formatDate(user.createdAt)}</dd>
              </div>
            </dl>
          </article>

          <aside className="profile-card profile-card--security">
            <ShieldCheck size={24} />
            <div>
              <strong>세션으로 안전하게 로그인 중</strong>
              <p>브라우저를 새로고침해도 로그인 상태가 유지됩니다.</p>
            </div>
            <button
              type="button"
              className="button button--secondary button--md button--full"
              onClick={handleLogout}
              disabled={logoutMutation.isPending}
            >
              <LogOut size={17} />
              {logoutMutation.isPending ? '로그아웃 중' : '로그아웃'}
            </button>
          </aside>
        </div>
      </div>
    </section>
  )
}
