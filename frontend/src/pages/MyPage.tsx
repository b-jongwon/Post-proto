import { useQuery } from '@tanstack/react-query'
import {
  BadgeCheck,
  Bell,
  CalendarDays,
  FileText,
  Heart,
  LayoutDashboard,
  LogOut,
  Mail,
  MessageCircle,
  RotateCcw,
  ShieldCheck,
  UserRound,
} from 'lucide-react'
import { useState } from 'react'
import {
  Link,
  useNavigate,
} from 'react-router-dom'
import {
  useLogout,
  useSession,
} from '@/features/auth/hooks/useAuth'
import { getMyDashboard } from '@/features/profile/profileApi'

type ActivityTab = 'posts' | 'comments' | 'likes'

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(new Date(value))
}
function formatShortDate(value: string): string {
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(value))
}

export function MyPage() {
  const [activeTab, setActiveTab] =
    useState<ActivityTab>('posts')
  const navigate = useNavigate()
  const session = useSession()
  const logoutMutation = useLogout()
  const user = session.data

  const dashboardQuery = useQuery({
    queryKey: ['me', 'dashboard'],
    queryFn: ({ signal }) =>
      getMyDashboard(signal),
    enabled: Boolean(user),
    retry: false,
  })

  if (!user) {
    return null
  }

  const dashboard = dashboardQuery.data
  const profile = dashboard?.profile ?? user

  const handleLogout = async () => {
    await logoutMutation.mutateAsync()
    navigate('/', { replace: true })
  }

  return (
    <section className="profile-page profile-dashboard">
      <div className="container profile-page__inner">
        <div className="profile-heading">
          <span className="auth-step">
            MY FACTHUB
          </span>
          <h1>내 활동 대시보드</h1>
          <p>
            프로필과 내가 만든 커뮤니티 활동을 한곳에서
            확인합니다.
          </p>
        </div>

        <article className="profile-hero-card">
          <div
            className="profile-avatar profile-avatar--large"
            aria-hidden="true"
          >
            {user.nickname
              .slice(0, 1)
              .toUpperCase()}
          </div>
          <div className="profile-hero-card__identity">
            <div className="profile-name-line">
              <h2>
                {profile.fullName || profile.nickname}
              </h2>
              <span className="status-pill">
                <BadgeCheck size={14} />
                이메일 인증
              </span>
            </div>
            <p>
              @{profile.nickname} · {profile.email}
            </p>
            <div className="profile-hero-card__meta">
              {profile.age !== null && (
                <span>
                  {profile.birthYear}년생 · {profile.age}세
                </span>
              )}
              <span>
                {profile.role === 'ADMIN'
                  ? '관리자'
                  : '일반 사용자'}
              </span>
              <span>
                {formatDate(profile.createdAt)} 가입
              </span>
            </div>
          </div>
          <div className="profile-hero-card__actions">
            {profile.role === 'ADMIN' && (
              <Link
                to="/admin"
                className="button button--primary button--md"
              >
                <LayoutDashboard size={17} />
                관리자 센터
              </Link>
            )}
            <Link
              to="/notifications"
              className="button button--secondary button--md"
            >
              <Bell size={17} />
              알림
              {(dashboard?.unreadNotificationCount ?? 0) > 0
                && (
                <strong className="button-count">
                  {dashboard?.unreadNotificationCount}
                </strong>
              )}
            </Link>
          </div>
        </article>

        <div className="profile-stat-grid">
          <button
            type="button"
            className={activeTab === 'posts' ? 'is-active' : ''}
            onClick={() => setActiveTab('posts')}
          >
            <FileText size={21} />
            <span>내가 작성한 글</span>
            <strong>{dashboard?.postCount ?? 0}</strong>
          </button>
          <button
            type="button"
            className={activeTab === 'comments' ? 'is-active' : ''}
            onClick={() => setActiveTab('comments')}
          >
            <MessageCircle size={21} />
            <span>내 댓글</span>
            <strong>{dashboard?.commentCount ?? 0}</strong>
          </button>
          <button
            type="button"
            className={activeTab === 'likes' ? 'is-active' : ''}
            onClick={() => setActiveTab('likes')}
          >
            <Heart size={21} />
            <span>좋아요한 글</span>
            <strong>{dashboard?.likedPostCount ?? 0}</strong>
          </button>
        </div>

        <div className="profile-dashboard-grid">
          <article className="profile-card profile-activity-card">
            <header>
              <div>
                <span>ACTIVITY</span>
                <h2>
                  {activeTab === 'posts'
                    ? '내가 작성한 글'
                    : activeTab === 'comments'
                      ? '내가 작성한 댓글'
                      : '좋아요한 글'}
                </h2>
              </div>
              <small>최근 20개</small>
            </header>

            {dashboardQuery.isPending && (
              <div className="profile-activity-state">
                활동을 불러오고 있습니다.
              </div>
            )}

            {dashboardQuery.isError && (
              <div className="profile-activity-state profile-activity-state--error">
                <p>활동을 불러오지 못했습니다.</p>
                <button
                  type="button"
                  onClick={() =>
                    void dashboardQuery.refetch()
                  }
                >
                  <RotateCcw size={15} />
                  다시 시도
                </button>
              </div>
            )}

            {dashboard && activeTab === 'comments' && (
              <div className="profile-activity-list">
                {dashboard.comments.map((comment) => (
                  <Link
                    key={comment.commentId}
                    to={`/posts/${comment.postId}`}
                    className="profile-comment-item"
                  >
                    <span>
                      <MessageCircle size={16} />
                    </span>
                    <div>
                      <strong>{comment.postTitle}</strong>
                      <p>{comment.content}</p>
                    </div>
                    <time>
                      {formatShortDate(comment.createdAt)}
                    </time>
                  </Link>
                ))}
                {dashboard.comments.length === 0 && (
                  <p className="profile-empty">
                    아직 작성한 댓글이 없습니다.
                  </p>
                )}
              </div>
            )}

            {dashboard && activeTab !== 'comments' && (
              <div className="profile-activity-list">
                {(activeTab === 'posts'
                  ? dashboard.posts
                  : dashboard.likedPosts
                ).map((post) => (
                  <Link
                    key={post.postId}
                    to={`/posts/${post.postId}`}
                    className="profile-post-item"
                  >
                    <span className="profile-post-item__category">
                      {post.category}
                    </span>
                    <div>
                      <strong>{post.title}</strong>
                      <p>{post.contentPreview}</p>
                      <small>
                        조회 {post.viewCount.toLocaleString('ko-KR')}
                        {' · '}좋아요 {post.likeCount.toLocaleString('ko-KR')}
                        {' · '}댓글 {post.commentCount.toLocaleString('ko-KR')}
                      </small>
                    </div>
                    <time>
                      {formatShortDate(post.createdAt)}
                    </time>
                  </Link>
                ))}
                {(activeTab === 'posts'
                  ? dashboard.posts
                  : dashboard.likedPosts
                ).length === 0 && (
                  <p className="profile-empty">
                    {activeTab === 'posts'
                      ? '아직 작성한 글이 없습니다.'
                      : '아직 좋아요한 글이 없습니다.'}
                  </p>
                )}
              </div>
            )}
          </article>

          <aside className="profile-dashboard-side">
            <article className="profile-card profile-card--details">
              <h2>계정 정보</h2>
              <dl className="profile-details">
                <div>
                  <dt><Mail size={17} /> 이메일</dt>
                  <dd>{profile.email}</dd>
                </div>
                <div>
                  <dt><UserRound size={17} /> 닉네임</dt>
                  <dd>{profile.nickname}</dd>
                </div>
                <div>
                  <dt><ShieldCheck size={17} /> 권한</dt>
                  <dd>
                    {profile.role === 'ADMIN'
                      ? '관리자'
                      : '일반 사용자'}
                  </dd>
                </div>
                <div>
                  <dt><CalendarDays size={17} /> 가입일</dt>
                  <dd>{formatDate(profile.createdAt)}</dd>
                </div>
              </dl>
            </article>

            <article className="profile-card profile-card--security">
              <ShieldCheck size={24} />
              <div>
                <strong>안전한 세션 로그인</strong>
                <p>
                  인증된 이메일과 서버 세션으로 로그인
                  상태를 보호합니다.
                </p>
              </div>
              <button
                type="button"
                className="button button--secondary button--md button--full"
                onClick={handleLogout}
                disabled={logoutMutation.isPending}
              >
                <LogOut size={17} />
                {logoutMutation.isPending
                  ? '로그아웃 중'
                  : '로그아웃'}
              </button>
            </article>
          </aside>
        </div>
      </div>
    </section>
  )
}
