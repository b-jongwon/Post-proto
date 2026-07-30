import {
  Activity,
  Ban,
  Eye,
  EyeOff,
  FileText,
  Heart,
  LoaderCircle,
  MessageCircle,
  Play,
  RotateCcw,
  ShieldCheck,
  UserCheck,
  Users,
} from 'lucide-react'
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { useState } from 'react'
import {
  Link,
  Navigate,
} from 'react-router-dom'
import { useSession } from '@/features/auth/hooks/useAuth'
import {
  changeAdminPostVisibility,
  changeAdminUserStatus,
  forceAdminAnalysis,
  getAdminDashboard,
  getAdminPosts,
  getAdminUsers,
} from '@/features/admin/adminApi'

type AdminTab = 'posts' | 'users'

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(new Date(value))
}

export function AdminPage() {
  const [activeTab, setActiveTab] =
    useState<AdminTab>('posts')
  const queryClient = useQueryClient()
  const session = useSession()
  const user = session.data

  const enabled = user?.role === 'ADMIN'

  const dashboardQuery = useQuery({
    queryKey: ['admin', 'dashboard'],
    queryFn: ({ signal }) =>
      getAdminDashboard(signal),
    enabled,
    retry: false,
  })
  const postsQuery = useQuery({
    queryKey: ['admin', 'posts'],
    queryFn: ({ signal }) =>
      getAdminPosts(signal),
    enabled,
    retry: false,
  })
  const usersQuery = useQuery({
    queryKey: ['admin', 'users'],
    queryFn: ({ signal }) =>
      getAdminUsers(signal),
    enabled,
    retry: false,
  })

  const visibilityMutation = useMutation({
    mutationFn: ({
      postId,
      hidden,
    }: {
      postId: number
      hidden: boolean
    }) => changeAdminPostVisibility(postId, hidden),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: ['admin'],
      })
      void queryClient.invalidateQueries({
        queryKey: ['posts'],
      })
    },
  })

  const userStatusMutation = useMutation({
    mutationFn: ({
      userId,
      suspended,
    }: {
      userId: number
      suspended: boolean
    }) =>
      changeAdminUserStatus(
        userId,
        suspended ? 'SUSPENDED' : 'ACTIVE',
      ),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: ['admin'],
      })
    },
  })

  const analysisMutation = useMutation({
    mutationFn: (postId: number) =>
      forceAdminAnalysis(postId),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: ['posts'],
      })
    },
  })

  if (!session.isPending && !enabled) {
    return <Navigate to="/" replace />
  }

  const dashboard = dashboardQuery.data
  const stats = [
    {
      label: '전체 회원',
      value: dashboard?.totalUserCount ?? 0,
      icon: Users,
    },
    {
      label: '공개 게시글',
      value: dashboard?.publishedPostCount ?? 0,
      icon: FileText,
    },
    {
      label: '총 좋아요',
      value: dashboard?.totalLikeCount ?? 0,
      icon: Heart,
    },
    {
      label: '총 댓글',
      value: dashboard?.totalCommentCount ?? 0,
      icon: MessageCircle,
    },
    {
      label: '오늘 작성',
      value: dashboard?.todayPostCount ?? 0,
      icon: Activity,
    },
  ]

  return (
    <section className="admin-page">
      <div className="container admin-page__inner">
        <header className="page-hero-heading admin-heading">
          <div>
            <span>FACTHUB CONTROL CENTER</span>
            <h1>관리자 센터</h1>
            <p>
              커뮤니티 상태와 회원, 게시글, AI 분석을
              안전하게 관리합니다.
            </p>
          </div>
          <span className="admin-heading__badge">
            <ShieldCheck size={18} />
            관리자 전용
          </span>
        </header>

        <div className="admin-stat-grid">
          {stats.map((stat) => {
            const Icon = stat.icon
            return (
              <article key={stat.label}>
                <span><Icon size={20} /></span>
                <p>{stat.label}</p>
                <strong>
                  {stat.value.toLocaleString('ko-KR')}
                </strong>
              </article>
            )
          })}
        </div>

        <div className="admin-tabs">
          <button
            type="button"
            className={activeTab === 'posts' ? 'is-active' : ''}
            onClick={() => setActiveTab('posts')}
          >
            <FileText size={17} />
            게시글 관리
          </button>
          <button
            type="button"
            className={activeTab === 'users' ? 'is-active' : ''}
            onClick={() => setActiveTab('users')}
          >
            <Users size={17} />
            회원 관리
          </button>
        </div>

        <section className="admin-table-card">
          {activeTab === 'posts' ? (
            <>
              <header>
                <div>
                  <span>POST MODERATION</span>
                  <h2>게시글 관리</h2>
                </div>
                <small>
                  숨김 {dashboard?.hiddenPostCount ?? 0}건
                </small>
              </header>

              {postsQuery.isPending ? (
                <div className="admin-state">
                  게시글을 불러오고 있습니다.
                </div>
              ) : postsQuery.isError ? (
                <div className="admin-state">
                  <button
                    type="button"
                    onClick={() =>
                      void postsQuery.refetch()
                    }
                  >
                    <RotateCcw size={16} />
                    다시 시도
                  </button>
                </div>
              ) : (
                <div className="admin-table-wrap">
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>게시글</th>
                        <th>활동</th>
                        <th>상태</th>
                        <th>관리</th>
                      </tr>
                    </thead>
                    <tbody>
                      {postsQuery.data?.content.map(
                        (post) => (
                          <tr key={post.postId}>
                            <td>
                              <Link
                                to={`/posts/${post.postId}`}
                              >
                                <strong>{post.title}</strong>
                                <span>
                                  {post.authorNickname}
                                  {' · '}
                                  {formatDate(post.createdAt)}
                                </span>
                              </Link>
                            </td>
                            <td>
                              조회 {post.viewCount}
                              <br />
                              좋아요 {post.likeCount}
                              {' · '}댓글 {post.commentCount}
                            </td>
                            <td>
                              <span
                                className={`admin-status admin-status--${post.status.toLowerCase()}`}
                              >
                                {post.status === 'PUBLISHED'
                                  ? '공개'
                                  : '숨김'}
                              </span>
                            </td>
                            <td>
                              <div className="admin-row-actions">
                                <button
                                  type="button"
                                  onClick={() =>
                                    visibilityMutation.mutate({
                                      postId: post.postId,
                                      hidden:
                                        post.status
                                        === 'PUBLISHED',
                                    })
                                  }
                                  disabled={
                                    visibilityMutation.isPending
                                  }
                                >
                                  {post.status === 'PUBLISHED'
                                    ? <EyeOff size={15} />
                                    : <Eye size={15} />}
                                  {post.status === 'PUBLISHED'
                                    ? '숨김'
                                    : '공개'}
                                </button>
                                <button
                                  type="button"
                                  onClick={() =>
                                    analysisMutation.mutate(
                                      post.postId,
                                    )
                                  }
                                  disabled={
                                    analysisMutation.isPending
                                    || post.status !== 'PUBLISHED'
                                  }
                                >
                                  {analysisMutation.isPending ? (
                                    <LoaderCircle
                                      className="spin"
                                      size={15}
                                    />
                                  ) : (
                                    <Play size={15} />
                                  )}
                                  AI 실행
                                </button>
                              </div>
                            </td>
                          </tr>
                        ),
                      )}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          ) : (
            <>
              <header>
                <div>
                  <span>USER MANAGEMENT</span>
                  <h2>회원 관리</h2>
                </div>
                <small>
                  정지 {dashboard?.suspendedUserCount ?? 0}명
                </small>
              </header>

              {usersQuery.isPending ? (
                <div className="admin-state">
                  회원을 불러오고 있습니다.
                </div>
              ) : usersQuery.isError ? (
                <div className="admin-state">
                  <button
                    type="button"
                    onClick={() =>
                      void usersQuery.refetch()
                    }
                  >
                    <RotateCcw size={16} />
                    다시 시도
                  </button>
                </div>
              ) : (
                <div className="admin-table-wrap">
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>회원</th>
                        <th>프로필</th>
                        <th>상태</th>
                        <th>관리</th>
                      </tr>
                    </thead>
                    <tbody>
                      {usersQuery.data?.content.map(
                        (member) => (
                          <tr key={member.userId}>
                            <td>
                              <strong>{member.nickname}</strong>
                              <span>{member.email}</span>
                            </td>
                            <td>
                              {member.fullName ?? '미등록'}
                              {member.age !== null
                                ? ` · ${member.age}세`
                                : ''}
                              <br />
                              <small>
                                {formatDate(member.createdAt)} 가입
                              </small>
                            </td>
                            <td>
                              <span
                                className={`admin-status admin-status--${member.status.toLowerCase()}`}
                              >
                                {member.role === 'ADMIN'
                                  ? '관리자'
                                  : member.status === 'ACTIVE'
                                    ? '활성'
                                    : '정지'}
                              </span>
                            </td>
                            <td>
                              {member.role === 'ADMIN' ? (
                                <span className="admin-protected">
                                  <ShieldCheck size={15} />
                                  보호 계정
                                </span>
                              ) : (
                                <button
                                  type="button"
                                  className="admin-user-status-button"
                                  onClick={() =>
                                    userStatusMutation.mutate({
                                      userId: member.userId,
                                      suspended:
                                        member.status
                                        === 'ACTIVE',
                                    })
                                  }
                                  disabled={
                                    userStatusMutation.isPending
                                  }
                                >
                                  {member.status === 'ACTIVE'
                                    ? <Ban size={15} />
                                    : <UserCheck size={15} />}
                                  {member.status === 'ACTIVE'
                                    ? '회원 정지'
                                    : '정지 해제'}
                                </button>
                              )}
                            </td>
                          </tr>
                        ),
                      )}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </section>
  )
}

