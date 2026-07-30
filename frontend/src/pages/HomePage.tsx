import {
    type CSSProperties,
    type FormEvent,
    useState,
} from 'react'
import {useQuery} from '@tanstack/react-query'
import {
    ArrowRight,
    BarChart3,
    Bookmark,
    Check,
    ChevronRight,
    Clock3,
    Eye,
    Heart,
    MessageCircle,
    RotateCcw,
    Search,
    ShieldCheck,
    Sparkles,
    TrendingUp,
} from 'lucide-react'
import {Link} from 'react-router-dom'
import {Badge} from '@/components/ui/Badge'
import {HomeHighlights} from '@/components/home/HomeHighlights'
import {useSession} from '@/features/auth/hooks/useAuth'
import {
    getPostErrorMessage,
    getPosts,
    getPostStatistics,
} from '@/features/posts/postApi'
import type {PostSummary} from '@/features/posts/postTypes'

const trendingTopics = [
    '#AI뉴스',
    '#생활상식',
    '#경제지표',
    '#건강정보',
    '#자동차',
]

type BadgeTone =
    | 'brand'
    | 'success'
    | 'warning'
    | 'danger'

interface HomeAnalysisPresentation {
    badgeLabel: string
    badgeTone: BadgeTone
    score: number | null
    statusLabel: string
    detailLabel: string
}

function getAnalysisPresentation(
    post: PostSummary,
): HomeAnalysisPresentation {
    if (post.analysisStale) {
        return {
            badgeLabel: '재검증 필요',
            badgeTone: 'warning',
            score: post.confidenceScore,
            statusLabel: '게시글 수정 후 재검증 대기',
            detailLabel: '이전 판정 확신도',
        }
    }

    if (post.analysisStatus !== 'COMPLETED') {
        return {
            badgeLabel: '분석 전',
            badgeTone: 'brand',
            score: null,
            statusLabel: 'AI 분석 전',
            detailLabel: 'AI 분석 대기',
        }
    }

    switch (post.analysisVerdict) {
        case 'TRUE':
            return {
                badgeLabel: '사실',
                badgeTone: 'success',
                score: post.confidenceScore,
                statusLabel: 'AI 분석 완료',
                detailLabel: '판정 확신도',
            }

        case 'MOSTLY_TRUE':
            return {
                badgeLabel: '대체로 사실',
                badgeTone: 'success',
                score: post.confidenceScore,
                statusLabel: 'AI 분석 완료',
                detailLabel: '판정 확신도',
            }

        case 'MIXED':
            return {
                badgeLabel: '혼합',
                badgeTone: 'warning',
                score: post.confidenceScore,
                statusLabel: 'AI 분석 완료',
                detailLabel: '판정 확신도',
            }

        case 'MOSTLY_FALSE':
            return {
                badgeLabel: '대체로 거짓',
                badgeTone: 'danger',
                score: post.confidenceScore,
                statusLabel: 'AI 분석 완료',
                detailLabel: '판정 확신도',
            }

        case 'FALSE':
            return {
                badgeLabel: '거짓',
                badgeTone: 'danger',
                score: post.confidenceScore,
                statusLabel: 'AI 분석 완료',
                detailLabel: '판정 확신도',
            }

        case 'UNVERIFIABLE':
            return {
                badgeLabel: '판단 유보',
                badgeTone: 'brand',
                score: post.confidenceScore,
                statusLabel: 'AI 분석 완료',
                detailLabel: '판정 확신도',
            }

        default:
            return {
                badgeLabel: '분석 완료',
                badgeTone: 'brand',
                score: post.confidenceScore,
                statusLabel: 'AI 분석 완료',
                detailLabel: '판정 확신도',
            }
    }
}

function ScoreRing({
                       score,
                   }: {
    score: number
}) {
    return (
        <div
            className="score-ring"
            style={
                {
                    '--score': `${score * 3.6}deg`,
                } as CSSProperties
            }
        >
            <div className="score-ring__inner">
                <strong>{score}</strong>
                <span>확신도</span>
            </div>
        </div>
    )
}

function formatRelativeTime(
    value: string,
): string {
    const date = new Date(value)

    if (Number.isNaN(date.getTime())) {
        return value
    }

    const diffMilliseconds =
        Date.now() - date.getTime()

    if (diffMilliseconds < 0) {
        return '방금 전'
    }

    const minutes = Math.floor(
        diffMilliseconds / 60_000,
    )

    if (minutes < 1) {
        return '방금 전'
    }

    if (minutes < 60) {
        return `${minutes}분 전`
    }

    const hours = Math.floor(minutes / 60)

    if (hours < 24) {
        return `${hours}시간 전`
    }

    const days = Math.floor(hours / 24)

    if (days < 7) {
        return `${days}일 전`
    }

    return new Intl.DateTimeFormat('ko-KR', {
        month: 'short',
        day: 'numeric',
    }).format(date)
}

function getPostExcerpt(
    post: PostSummary,
): string {
    const source =
        (
            post.analysisStatus === 'COMPLETED'
            && !post.analysisStale
            && post.analysisSummary
        )
            ? post.analysisSummary
            : post.contentPreview
            ?? post.content
            ?? '게시글 상세 페이지에서 검증할 주장과 내용을 확인할 수 있습니다.'

    const normalized = source
        .replace(/\s+/g, ' ')
        .trim()

    if (normalized.length <= 115) {
        return normalized
    }

    return `${normalized.slice(0, 115)}...`
}

function HomeFeedSkeleton() {
    return (
        <>
            {Array.from({
                length: 3,
            }).map((_, index) => (
                <article
                    key={index}
                    className="feed-card home-feed-skeleton-card"
                    aria-hidden="true"
                >
                    <div className="feed-card__body">
                        <div className="home-feed-skeleton-meta">
                            <span className="home-feed-skeleton-block home-feed-skeleton-block--badge"/>
                            <span className="home-feed-skeleton-block home-feed-skeleton-block--time"/>
                        </div>

                        <span className="home-feed-skeleton-block home-feed-skeleton-block--title"/>
                        <span className="home-feed-skeleton-block home-feed-skeleton-block--text"/>
                        <span className="home-feed-skeleton-block home-feed-skeleton-block--text-short"/>

                        <div className="home-feed-skeleton-author">
                            <span className="home-feed-skeleton-block home-feed-skeleton-block--avatar"/>
                            <span className="home-feed-skeleton-block home-feed-skeleton-block--author"/>
                        </div>
                    </div>

                    <div className="feed-card__verdict home-feed-skeleton-verdict">
                        <span className="home-feed-skeleton-block home-feed-skeleton-block--badge"/>
                        <span className="home-feed-skeleton-block home-feed-skeleton-block--score"/>
                        <span className="home-feed-skeleton-block home-feed-skeleton-block--time"/>
                    </div>
                </article>
            ))}
        </>
    )
}

export function HomePage() {
    const session = useSession()
    const user = session.data

    const [searchInput, setSearchInput] =
        useState('')
    const [activeKeyword, setActiveKeyword] =
        useState('')
    const [showAll, setShowAll] =
        useState(false)

    const pageSize = showAll ? 10 : 3

    const postsQuery = useQuery({
        queryKey: [
            'posts',
            'home',
            activeKeyword,
            pageSize,
        ],
        queryFn: ({signal}) =>
            getPosts(
                {
                    page: 0,
                    size: pageSize,
                    keyword:
                        activeKeyword || undefined,
                    sort: 'latest',
                },
                signal,
            ),
        retry: false,
    })


    const statisticsQuery = useQuery({
        queryKey: [
            'posts',
            'statistics',
        ],
        queryFn: ({signal}) =>
            getPostStatistics(signal),
        retry: false,
    })

    const posts =
        postsQuery.data?.content ?? []

    const totalElements =
        postsQuery.data?.totalElements ?? 0

    const totalPostCount =
        statisticsQuery.data?.totalPostCount ?? 0

    const completedVerificationCount =
        statisticsQuery.data
            ?.completedVerificationCount ?? 0

    const totalLikeCount =
        statisticsQuery.data?.totalLikeCount ?? 0

    const totalCommentCount =
        statisticsQuery.data?.totalCommentCount ?? 0

    const todayPostCount =
        statisticsQuery.data?.todayPostCount ?? 0

    const pendingVerificationCount =
        statisticsQuery.data
            ?.pendingVerificationCount ?? 0

    const progressWidth =
        totalPostCount > 0
            ? Math.min(
                100,
                Math.max(
                    8,
                    (
                        completedVerificationCount
                        / totalPostCount
                    ) * 100,
                ),
            )
            : 0

    const handleSearch = (
        event: FormEvent<HTMLFormElement>,
    ) => {
        event.preventDefault()

        setActiveKeyword(
            searchInput.trim(),
        )
        setShowAll(false)
    }

    const handleTrendingSearch = (
        topic: string,
    ) => {
        const keyword = topic.replace(
            /^#/,
            '',
        )

        setSearchInput(keyword)
        setActiveKeyword(keyword)
        setShowAll(false)

        window.setTimeout(() => {
            document
                .getElementById('discover')
                ?.scrollIntoView({
                    behavior: 'smooth',
                })
        }, 0)
    }

    const handleClearSearch = () => {
        setSearchInput('')
        setActiveKeyword('')
        setShowAll(false)
    }

    return (
        <div className="home-page">
            <section className="hero-section">
                <div className="hero-section__glow hero-section__glow--one"/>
                <div className="hero-section__glow hero-section__glow--two"/>

                <div className="container hero-section__inner">
                    <div className="hero-copy">
                        <Badge tone="brand">
                            <Sparkles size={14}/>
                            AI 기반 출처 검증
                        </Badge>

                        <h1>
                            정보 속에서
                            <br/>
                            <span>
                                근거를 확인하세요
              </span>
                        </h1>

                        <p>
                            FactHub는 게시글의 핵심 주장을
                            분리하고, 신뢰할 수 있는 출처를
                            연결해 판단에 필요한 맥락을
                            보여줍니다.
                        </p>

                        <form
                            className="hero-search"
                            role="search"
                            onSubmit={handleSearch}
                        >
                            <Search
                                size={21}
                                aria-hidden="true"
                            />

                            <input
                                type="search"
                                value={searchInput}
                                onChange={(event) =>
                                    setSearchInput(
                                        event.target.value,
                                    )
                                }
                                placeholder="확인하고 싶은 주장이나 이슈를 검색해보세요"
                                aria-label="팩트체크 검색"
                            />

                            <button type="submit">
                                검색
                            </button>
                        </form>

                        <div
                            className="hero-trending"
                            aria-label="인기 검색어"
                        >
              <span>
                지금 많이 찾는 주제
              </span>

                            <div>
                                {trendingTopics.map(
                                    (topic) => (
                                        <button
                                            key={topic}
                                            type="button"
                                            onClick={() =>
                                                handleTrendingSearch(
                                                    topic,
                                                )
                                            }
                                        >
                                            {topic}
                                        </button>
                                    ),
                                )}
                            </div>
                        </div>
                    </div>

                    <div
                        className="hero-visual"
                        aria-label="팩트체크 분석 예시"
                    >
                        <div className="analysis-card analysis-card--main">
                            <div className="analysis-card__top">
                <span className="analysis-card__eyebrow">
                  <ShieldCheck size={16}/>
                  FactHub 분석
                </span>

                                <Badge tone="danger">
                                    거짓
                                </Badge>
                            </div>

                            <h2>
                                대한민국의 수도는 부산이다
                            </h2>

                            <p>
                                공식 기록과 정부 통계에서
                                확인된 내용과 일치하지
                                않습니다.
                            </p>

                            <div className="analysis-card__score">
                                <ScoreRing score={96}/>

                                <div>
                                    <strong>
                                        핵심 주장 2개 검증
                                    </strong>
                                    <span>
                    공공기관 출처 2개 연결
                  </span>
                                </div>
                            </div>

                            <div className="analysis-card__claim">
                <span>
                  <Check size={14}/>
                  주장 분리
                </span>
                                <span>
                  <Check size={14}/>
                  근거 연결
                </span>
                                <span>
                  <Check size={14}/>
                  출처 공개
                </span>
                            </div>
                        </div>

                        <div className="analysis-float analysis-float--source">
              <span className="analysis-float__icon">
                <Bookmark size={17}/>
              </span>

                            <div>
                                <strong>
                                    공식 출처 우선
                                </strong>
                                <span>
                  국가기록원 · e-나라지표
                </span>
                            </div>
                        </div>

                        <div className="analysis-float analysis-float--trend">
              <span className="analysis-float__icon">
                <TrendingUp size={17}/>
              </span>

                            <div>
                                <strong>
                                    실시간 분석
                                </strong>
                                <span>
                  근거와 맥락을 한눈에
                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <HomeHighlights/>

            <section
                className="home-stats-strip"
                aria-label="FactHub 서비스 통계"
            >
                <div className="container home-stats-strip__grid">
                    <article>
                        <span><ShieldCheck size={19}/></span>
                        <div>
                            <small>총 게시글</small>
                            <strong>
                                {statisticsQuery.isPending
                                    ? '—'
                                    : totalPostCount.toLocaleString('ko-KR')}
                            </strong>
                        </div>
                    </article>
                    <article>
                        <span><Heart size={19}/></span>
                        <div>
                            <small>총 좋아요</small>
                            <strong>
                                {statisticsQuery.isPending
                                    ? '—'
                                    : totalLikeCount.toLocaleString('ko-KR')}
                            </strong>
                        </div>
                    </article>
                    <article>
                        <span><MessageCircle size={19}/></span>
                        <div>
                            <small>총 댓글</small>
                            <strong>
                                {statisticsQuery.isPending
                                    ? '—'
                                    : totalCommentCount.toLocaleString('ko-KR')}
                            </strong>
                        </div>
                    </article>
                    <article>
                        <span><Clock3 size={19}/></span>
                        <div>
                            <small>오늘 작성</small>
                            <strong>
                                {statisticsQuery.isPending
                                    ? '—'
                                    : todayPostCount.toLocaleString('ko-KR')}
                            </strong>
                        </div>
                    </article>
                </div>
            </section>

            <section
                className="content-section"
                id="discover"
            >
                <div className="container content-grid">
                    <div className="feed-column">
                        <div className="section-heading">
                            <div>
                <span className="section-heading__eyebrow">
                  {activeKeyword
                      ? '검색 결과'
                      : '최신 검증 요청'}
                </span>

                                <h2>
                                    {activeKeyword
                                        ? `"${activeKeyword}" 관련 게시글`
                                        : '최근 등록된 팩트체크'}
                                </h2>
                            </div>

                            <div className="home-feed-heading-actions">
                                {activeKeyword && (
                                    <button
                                        type="button"
                                        className="text-button"
                                        onClick={handleClearSearch}
                                    >
                                        검색 초기화
                                    </button>
                                )}

                                <button
                                    type="button"
                                    className="text-button"
                                    onClick={() =>
                                        setShowAll(
                                            (previous) =>
                                                !previous,
                                        )
                                    }
                                    disabled={
                                        totalElements <= 3
                                        && !showAll
                                    }
                                >
                                    {showAll
                                        ? '간단히 보기'
                                        : '전체보기'}
                                    <ArrowRight size={17}/>
                                </button>
                            </div>
                        </div>

                        <div className="feed-list">
                            {postsQuery.isPending && (
                                <HomeFeedSkeleton/>
                            )}

                            {postsQuery.isError && (
                                <div className="home-feed-state">
                  <span className="home-feed-state__icon">
                    <ShieldCheck
                        size={25}
                    />
                  </span>

                                    <h3>
                                        게시글을 불러오지
                                        못했습니다.
                                    </h3>

                                    <p>
                                        {getPostErrorMessage(
                                            postsQuery.error,
                                        )}
                                    </p>

                                    <button
                                        type="button"
                                        onClick={() =>
                                            void postsQuery.refetch()
                                        }
                                    >
                                        <RotateCcw size={16}/>
                                        다시 시도
                                    </button>
                                </div>
                            )}

                            {!postsQuery.isPending
                                && !postsQuery.isError
                                && posts.length === 0 && (
                                    <div className="home-feed-state">
                    <span className="home-feed-state__icon">
                      <Search size={25}/>
                    </span>

                                        <h3>
                                            표시할 게시글이
                                            없습니다.
                                        </h3>

                                        <p>
                                            {activeKeyword
                                                ? '다른 검색어로 다시 검색해 보세요.'
                                                : '첫 번째 검증 요청을 작성해 보세요.'}
                                        </p>

                                        <Link
                                            to={
                                                user
                                                    ? '/posts/new'
                                                    : '/login'
                                            }
                                        >
                                            글쓰기
                                            <ArrowRight
                                                size={16}
                                            />
                                        </Link>
                                    </div>
                                )}

                            {!postsQuery.isPending
                                && !postsQuery.isError
                                && posts.map((post) => {
                                    const presentation =
                                        getAnalysisPresentation(
                                            post,
                                        )

                                    return (
                                        <article
                                            key={post.postId}
                                            className="feed-card"
                                        >
                                            <Link
                                                to={`/posts/${post.postId}`}
                                                className="feed-card__body feed-card__body--link"
                                            >
                                                <div className="feed-card__meta">
                                                    <Badge>
                                                        {post.category}
                                                    </Badge>

                                                    <span>
                                                        <Clock3 size={14}/>
                                                        {formatRelativeTime(
                                                            post.createdAt,
                                                        )}
                                                    </span>
                                                </div>

                                                <h3>{post.title}</h3>

                                                <p>
                                                    {getPostExcerpt(
                                                        post,
                                                    )}
                                                </p>

                                                <div className="feed-card__author">
                                                    <span className="avatar">
                                                        {post.authorNickname
                                                            .slice(0, 1)
                                                            .toUpperCase()}
                                                    </span>

                                                    <span>
                                                        {post.authorNickname}
                                                    </span>
                                                </div>

                                                <div className="feed-card__stats">
                                                    <span>
                                                        <Eye size={15}/>
                                                        조회{' '}
                                                        {post.viewCount.toLocaleString(
                                                            'ko-KR',
                                                        )}
                                                    </span>
                                                    <span className="feed-card__like-count">
                                                           <Heart size={15}/>
                                                           좋아요{' '}
                                                          {(post.likeCount ?? 0).toLocaleString(
                                                            'ko-KR',
                                                        )}
                                                    </span>
                                                    <span>
                                                        <MessageCircle size={15}/>
                                                        댓글{' '}
                                                        {(post.commentCount ?? 0).toLocaleString(
                                                            'ko-KR',
                                                        )}
                                                    </span>
                                                    <span>
                                                        <ShieldCheck
                                                            size={15}
                                                        />
                                                        {presentation.statusLabel}
                                                    </span>
                                                </div>
                                            </Link>

                                            <div className="feed-card__verdict">
                                                <Badge
                                                    tone={presentation.badgeTone}
                                                >
                                                    {presentation.badgeLabel}
                                                </Badge>

                                                <strong>
                                                    {presentation.score
                                                        ?? '—'}
                                                </strong>

                                                <span>
                                                    {presentation.detailLabel}
                                                </span>

                                                {post.analysisCompletedAt
                                                    && !post.analysisStale && (
                                                        <small className="home-analysis-time">
                                                            {formatRelativeTime(
                                                                post.analysisCompletedAt,
                                                            )}
                                                        </small>
                                                    )}

                                                <Link
                                                    to={`/posts/${post.postId}`}
                                                    className="feed-card__detail-button"
                                                    aria-label={`${post.title} 상세 보기`}
                                                >
                                                    <ChevronRight
                                                        size={19}
                                                    />
                                                </Link>
                                            </div>
                                        </article>
                                    )
                                })}
                        </div>
                    </div>

                    <aside className="side-column">
                        <section
                            className="side-card principle-card"
                            id="service"
                        >
                            <div className="side-card__icon">
                                <ShieldCheck size={21}/>
                            </div>

                            <h3>
                                FactHub의 검증 원칙
                            </h3>

                            <p>
                                판정보다 근거를 먼저
                                보여드립니다.
                            </p>

                            <ul>
                                <li>
                                    <span>01</span>
                                    주장을 작게 나눠 검증해요
                                </li>
                                <li>
                                    <span>02</span>
                                    공식·1차 출처를 우선해요
                                </li>
                                <li>
                                    <span>03</span>
                                    과거 분석 이력을 남겨요
                                </li>
                            </ul>
                        </section>

                        <section className="side-card daily-card">
                            <div className="daily-card__header">
                                <div>
                  <span>
                    등록된 검증 요청
                  </span>

                                    <strong>
                                        {statisticsQuery.isPending
                                            ? '—'
                                            : pendingVerificationCount
                                                .toLocaleString(
                                                    'ko-KR',
                                                )}
                                    </strong>
                                </div>

                                <span className="daily-card__chart">
                  <BarChart3 size={24}/>
                </span>
                            </div>

                            <div className="daily-card__bar">
                <span
                    style={{
                        width: `${progressWidth}%`,
                    }}
                />
                            </div>

                            <p>
                                {statisticsQuery.isError
                                    ? '검증 현황을 불러오지 못했습니다.'
                                    : `전체 ${totalPostCount.toLocaleString(
                                        'ko-KR',
                                    )}건 중 ${completedVerificationCount.toLocaleString(
                                        'ko-KR',
                                    )}건 검증 완료`}
                            </p>
                        </section>

                        <section className="side-card cta-card">
              <span className="cta-card__spark">
                <Sparkles size={22}/>
              </span>

                            <h3>
                                확인하고 싶은 정보가
                                있나요?
                            </h3>

                            <p>
                                글을 작성하면 AI가 핵심
                                주장과 근거를 함께
                                분석합니다.
                            </p>

                            <Link
                                to={
                                    user
                                        ? '/posts/new'
                                        : '/signup'
                                }
                            >
                                {user
                                    ? '새 글 작성하기'
                                    : '무료로 시작하기'}
                                <ArrowRight size={16}/>
                            </Link>
                        </section>
                    </aside>
                </div>
            </section>
        </div>
    )
}
