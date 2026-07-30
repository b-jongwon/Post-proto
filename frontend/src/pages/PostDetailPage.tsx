import {
    type CSSProperties,
    useState,
} from 'react'
import {
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query'
import {
    ArrowLeft,
    Bookmark,
    CalendarDays,
    CheckCircle2,
    Copy,
    ExternalLink,
    Eye,
    Gauge,
    Heart,
    MessageCircle,
    History,
    Info,
    LoaderCircle,
    Pencil,
    RefreshCw,
    RotateCcw,
    Share2,
    ShieldCheck,
    Sparkles,
    Trash2,
    UserRound,
    X,
} from 'lucide-react'
import {
    Link,
    useNavigate,
    useParams,
} from 'react-router-dom'
import {Badge} from '@/components/ui/Badge'
import {Button} from '@/components/ui/Button'
import {Skeleton} from '@/components/ui/Skeleton'
import {PostInteractions} from '@/components/posts/PostInteractions'
import {PostSharePanel} from '@/components/posts/PostSharePanel'
import {useSession} from '@/features/auth/hooks/useAuth'
import {
    changeRepresentativeAnalysis,
    getFactCheckAnalysis,
    getFactCheckDetail,
    getFactCheckErrorMessage,
    getFactCheckHistory,
    runFactCheckAnalysis,
} from '@/features/factcheck/factCheckApi'
import type {
    FactCheckHistoryItem,
    FactCheckResponse,
    FactCheckVerdict,
} from '@/features/factcheck/factCheckTypes'
import {
    deletePost,
    getPost,
    getPostErrorMessage,
} from '@/features/posts/postApi'
import '@/styles/post-detail.css'
import '@/styles/fact-check.css'

type BadgeTone =
    | 'brand'
    | 'success'
    | 'warning'
    | 'danger'

interface VerdictPresentation {
    label: string
    description: string
    tone: BadgeTone
}

function formatDate(
    value: string,
): string {
    const date = new Date(value)

    if (Number.isNaN(date.getTime())) {
        return value
    }

    return new Intl.DateTimeFormat(
        'ko-KR',
        {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        },
    ).format(date)
}

function getVerdictPresentation(
    verdict: FactCheckVerdict | null,
): VerdictPresentation {
    switch (verdict) {
        case 'TRUE':
            return {
                label: '사실',
                description:
                    '검증된 출처와 대체로 일치하는 주장입니다.',
                tone: 'success',
            }

        case 'MOSTLY_TRUE':
            return {
                label: '대체로 사실',
                description:
                    '핵심 내용은 사실이지만 일부 맥락이나 표현에 보완이 필요합니다.',
                tone: 'success',
            }

        case 'MIXED':
            return {
                label: '혼합',
                description:
                    '사실인 부분과 사실이 아니거나 불확실한 부분이 함께 있습니다.',
                tone: 'warning',
            }

        case 'MOSTLY_FALSE':
            return {
                label: '대체로 거짓',
                description:
                    '일부 사실이 포함되어 있지만 핵심 주장은 부정확합니다.',
                tone: 'danger',
            }

        case 'FALSE':
            return {
                label: '거짓',
                description:
                    '검증된 출처와 핵심 주장이 일치하지 않습니다.',
                tone: 'danger',
            }

        case 'UNVERIFIABLE':
            return {
                label: '판단 유보',
                description:
                    '현재 확보된 출처만으로 사실 여부를 판단하기 어렵습니다.',
                tone: 'brand',
            }

        default:
            return {
                label: '분석 결과',
                description:
                    '팩트체크 분석 결과를 확인해 주세요.',
                tone: 'brand',
            }
    }
}

function getSafeExternalUrl(
    value: string,
): string | null {
    try {
        const url = new URL(value)

        if (
            url.protocol !== 'http:'
            && url.protocol !== 'https:'
        ) {
            return null
        }

        return url.toString()
    } catch {
        return null
    }
}

function getUrlHost(
    value: string,
): string {
    try {
        return new URL(value)
            .hostname
            .replace(/^www\./, '')
    } catch {
        return '외부 출처'
    }
}

function AnalysisScore({
                           label,
                           score,
                           description,
                       }: {
    label: string
    score: number
    description: string
}) {
    const safeScore = Math.min(
        100,
        Math.max(0, score),
    )

    return (
        <div className="fact-check-score-card">
            <div
                className="fact-check-score-ring"
                style={
                    {
                        '--fact-score':
                            `${safeScore * 3.6}deg`,
                    } as CSSProperties
                }
            >
                <div className="fact-check-score-ring__inner">
                    <strong>{safeScore}</strong>
                    <span>/ 100</span>
                </div>
            </div>

            <div className="fact-check-score-copy">
                <span>{label}</span>
                <p>{description}</p>
            </div>
        </div>
    )
}

function PostDetailSkeleton() {
    return (
        <section
            className="post-detail-page"
            aria-label="게시글 불러오는 중"
        >
            <div className="container post-detail-grid">
                <article className="post-detail-card post-detail-card--loading">
                    <Skeleton
                        width="96px"
                        height="28px"
                        radius="999px"
                    />

                    <Skeleton
                        width="82%"
                        height="44px"
                        radius="12px"
                    />

                    <Skeleton
                        width="54%"
                        height="20px"
                        radius="8px"
                    />

                    <div className="post-detail-skeleton-lines">
                        <Skeleton
                            width="100%"
                            height="18px"
                            radius="7px"
                        />
                        <Skeleton
                            width="100%"
                            height="18px"
                            radius="7px"
                        />
                        <Skeleton
                            width="92%"
                            height="18px"
                            radius="7px"
                        />
                    </div>
                </article>

                <aside className="post-detail-side-card post-detail-side-card--loading">
                    <Skeleton
                        width="48px"
                        height="48px"
                        radius="14px"
                    />

                    <Skeleton
                        width="72%"
                        height="24px"
                        radius="8px"
                    />

                    <Skeleton
                        width="100%"
                        height="16px"
                        radius="7px"
                    />
                </aside>
            </div>
        </section>
    )
}

function FactCheckResultSection({
                                    analysis,
                                }: {
    analysis: FactCheckResponse
}) {
    const verdict =
        getVerdictPresentation(
            analysis.verdict,
        )

    const credibilityScore =
        analysis.credibilityScore ?? 0

    const confidenceScore =
        analysis.confidenceScore ?? 0

    return (
        <section
            id="fact-check-result"
            className={`fact-check-result fact-check-result--${
                analysis.verdict?.toLowerCase() ?? 'pending'
            }`}
        >
            <header className="fact-check-result__header">
                <div className="fact-check-result__heading">
          <span className="fact-check-result__icon">
            <ShieldCheck size={24}/>
          </span>

                    <div>
                        <span>
                            FACTHUB AI ANALYSIS · #{analysis.runNumber}
                        </span>
                        <h2>AI 팩트체크 결과</h2>
                    </div>
                </div>

                <div className="fact-check-result__badges">
                    {analysis.isStale && (
                        <Badge tone="warning">
                            오래된 분석
                        </Badge>
                    )}

                    <Badge tone={verdict.tone}>
                        {verdict.label}
                    </Badge>
                </div>
            </header>

            <div className={`fact-check-verdict-panel fact-check-verdict-panel--${
                analysis.verdict?.toLowerCase() ?? 'pending'
            }`}>
                <div>
                    <span>종합 판정</span>
                    <strong>{verdict.label}</strong>
                    <p>{verdict.description}</p>
                </div>

                <span className="fact-check-verdict-panel__mark">
          <Sparkles size={29}/>
        </span>
            </div>

            <div className="fact-check-score-grid">
                <AnalysisScore
                    label="사실성 점수"
                    score={credibilityScore}
                    description="검증된 출처와 게시글 내용이 일치하는 정도입니다."
                />

                <AnalysisScore
                    label="AI 확신도"
                    score={confidenceScore}
                    description="AI가 현재 판정에 대해 가지는 분석 확신도입니다."
                />
            </div>

            <section className="fact-check-content-card">
                <div className="fact-check-section-title">
          <span>
            <Sparkles size={18}/>
          </span>

                    <div>
                        <small>SUMMARY</small>
                        <h3>분석 요약</h3>
                    </div>
                </div>

                <p className="fact-check-summary">
                    {analysis.summary
                        || '분석 요약이 제공되지 않았습니다.'}
                </p>
            </section>

            <section className="fact-check-content-card">
                <div className="fact-check-section-title">
          <span>
            <Gauge size={18}/>
          </span>

                    <div>
                        <small>EXPLANATION</small>
                        <h3>상세 분석</h3>
                    </div>
                </div>

                <div className="fact-check-explanation">
                    {analysis.explanation
                        || '상세 설명이 제공되지 않았습니다.'}
                </div>
            </section>

            <section className="fact-check-content-card">
                <div className="fact-check-sources-heading">
                    <div className="fact-check-section-title">
            <span>
              <ExternalLink size={18}/>
            </span>

                        <div>
                            <small>SOURCES</small>
                            <h3>검증에 사용한 출처</h3>
                        </div>
                    </div>

                    <strong>
                        {analysis.sources.length}개
                    </strong>
                </div>

                {analysis.sources.length > 0 ? (
                    <ul className="fact-check-source-list">
                        {analysis.sources.map(
                            (source, index) => {
                                const safeUrl =
                                    getSafeExternalUrl(
                                        source.url,
                                    )

                                const content = (
                                    <>
                    <span className="fact-check-source-index">
                      {String(index + 1).padStart(
                          2,
                          '0',
                      )}
                    </span>

                                        <div className="fact-check-source-copy">
                      <span>
                        {getUrlHost(
                            source.url,
                        )}
                      </span>

                                            <strong>
                                                {source.title}
                                            </strong>

                                            {source.snippet && (
                                                <p>
                                                    {source.snippet}
                                                </p>
                                            )}
                                        </div>

                                        {safeUrl && (
                                            <ExternalLink
                                                size={17}
                                                className="fact-check-source-external"
                                            />
                                        )}
                                    </>
                                )

                                return (
                                    <li
                                        key={`${source.url}-${index}`}
                                    >
                                        {safeUrl ? (
                                            <a
                                                href={safeUrl}
                                                target="_blank"
                                                rel="noreferrer"
                                            >
                                                {content}
                                            </a>
                                        ) : (
                                            <div className="fact-check-source-disabled">
                                                {content}
                                            </div>
                                        )}
                                    </li>
                                )
                            },
                        )}
                    </ul>
                ) : (
                    <div className="fact-check-no-sources">
                        <Info size={20}/>

                        <p>
                            이 분석에는 공개 가능한
                            출처가 포함되지 않았습니다.
                        </p>
                    </div>
                )}
            </section>

            <footer className="fact-check-result__footer">
                <Info size={17}/>

                <p>{analysis.disclaimer}</p>

                <div>
                    {analysis.model && (
                        <span>
              모델 {analysis.model}
            </span>
                    )}

                    {analysis.completedAt && (
                        <span>
              분석 완료{' '}
                            {formatDate(
                                analysis.completedAt,
                            )}
            </span>
                    )}
                </div>
            </footer>
        </section>
    )
}


function getHistoryStatusLabel(
    item: FactCheckHistoryItem,
): string {
    if (item.status === 'PROCESSING') {
        return '분석 중'
    }

    if (item.status === 'FAILED') {
        return '분석 실패'
    }

    return getVerdictPresentation(
        item.verdict,
    ).label
}

function getHistoryTone(
    item: FactCheckHistoryItem,
): BadgeTone {
    if (item.status === 'PROCESSING') {
        return 'brand'
    }

    if (item.status === 'FAILED') {
        return 'danger'
    }

    if (item.isStale) {
        return 'warning'
    }

    return getVerdictPresentation(
        item.verdict,
    ).tone
}

function FactCheckHistorySection({
                                     history,
                                     representativeAnalysisId,
                                     viewedAnalysisId,
                                     canManage,
                                     isLoading,
                                     isError,
                                     error,
                                     isSelecting,
                                     onRetry,
                                     onView,
                                     onSelectRepresentative,
                                 }: {
    history: FactCheckHistoryItem[]
    representativeAnalysisId: number | null
    viewedAnalysisId: number | null
    canManage: boolean
    isLoading: boolean
    isError: boolean
    error: unknown
    isSelecting: boolean
    onRetry: () => void
    onView: (item: FactCheckHistoryItem) => void
    onSelectRepresentative: (
        analysisId: number,
    ) => void
}) {
    return (
        <section
            id="fact-check-history"
            className="fact-check-history"
        >
            <header className="fact-check-history__header">
                <div className="fact-check-history__title">
                    <span>
                        <History size={21}/>
                    </span>

                    <div>
                        <small>ANALYSIS HISTORY</small>
                        <h2>팩트체크 분석 이력</h2>
                    </div>
                </div>

                <strong>
                    {history.length}회
                </strong>
            </header>

            {isLoading ? (
                <div className="fact-check-history__state">
                    <LoaderCircle
                        size={22}
                        className="spin"
                    />
                    분석 이력을 불러오고 있습니다.
                </div>
            ) : isError ? (
                <div className="fact-check-history__state fact-check-history__state--error">
                    <Info size={20}/>
                    <span>
                        {getFactCheckErrorMessage(error)}
                    </span>
                    <button
                        type="button"
                        onClick={onRetry}
                    >
                        <RefreshCw size={15}/>
                        다시 시도
                    </button>
                </div>
            ) : history.length === 0 ? (
                <div className="fact-check-history__state">
                    아직 저장된 분석 이력이 없습니다.
                </div>
            ) : (
                <ol className="fact-check-history__list">
                    {history.map((item) => {
                        const isRepresentative =
                            item.analysisId
                            === representativeAnalysisId

                        const isViewed =
                            item.analysisId
                            === viewedAnalysisId
                            || (
                                viewedAnalysisId === null
                                && isRepresentative
                            )

                        const canView =
                            item.status === 'COMPLETED'

                        const canSelect =
                            canManage
                            && item.status === 'COMPLETED'
                            && !item.isStale
                            && !isRepresentative

                        return (
                            <li
                                key={item.analysisId}
                                className={[
                                    'fact-check-history-item',
                                    isViewed
                                        ? 'fact-check-history-item--viewed'
                                        : '',
                                ]
                                    .filter(Boolean)
                                    .join(' ')}
                            >
                                <div className="fact-check-history-item__top">
                                    <div>
                                        <strong>
                                            #{item.runNumber} 분석
                                        </strong>

                                        <span>
                                            {formatDate(
                                                item.createdAt,
                                            )}
                                        </span>
                                    </div>

                                    <div className="fact-check-history-item__badges">
                                        {isRepresentative && (
                                            <Badge tone="success">
                                                대표 분석
                                            </Badge>
                                        )}

                                        {item.isStale && (
                                            <Badge tone="warning">
                                                오래된 분석
                                            </Badge>
                                        )}

                                        <Badge
                                            tone={getHistoryTone(
                                                item,
                                            )}
                                        >
                                            {getHistoryStatusLabel(
                                                item,
                                            )}
                                        </Badge>
                                    </div>
                                </div>

                                <p>
                                    {item.status === 'FAILED'
                                        ? item.errorMessage
                                        || '분석 중 오류가 발생했습니다.'
                                        : item.summary
                                        || '분석 요약이 없습니다.'}
                                </p>

                                <div className="fact-check-history-item__meta">
                                    <span>
                                        요청자 {item.requestedByNickname}
                                    </span>

                                    {item.credibilityScore !== null && (
                                        <span>
                                            사실성 {item.credibilityScore}점
                                        </span>
                                    )}

                                    {item.confidenceScore !== null && (
                                        <span>
                                            확신도 {item.confidenceScore}점
                                        </span>
                                    )}
                                </div>

                                <div className="fact-check-history-item__actions">
                                    <button
                                        type="button"
                                        onClick={() =>
                                            onView(item)
                                        }
                                        disabled={!canView}
                                    >
                                        <Gauge size={15}/>
                                        {isViewed
                                            ? '현재 보고 있음'
                                            : canView
                                                ? '결과 보기'
                                                : '상세 없음'}
                                    </button>

                                    {canSelect && (
                                        <button
                                            type="button"
                                            className="fact-check-history-item__select"
                                            onClick={() =>
                                                onSelectRepresentative(
                                                    item.analysisId,
                                                )
                                            }
                                            disabled={isSelecting}
                                        >
                                            <ShieldCheck size={15}/>
                                            대표로 지정
                                        </button>
                                    )}
                                </div>
                            </li>
                        )
                    })}
                </ol>
            )}
        </section>
    )
}

export function PostDetailPage() {
    const {postId} = useParams()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const session = useSession()

    const parsedPostId = Number(postId)

    const isValidPostId =
        Number.isInteger(parsedPostId)
        && parsedPostId > 0

    const [isCopied, setIsCopied] =
        useState(false)

    const [
        isDeleteDialogOpen,
        setIsDeleteDialogOpen,
    ] = useState(false)

    const [
        viewedAnalysisId,
        setViewedAnalysisId,
    ] = useState<number | null>(null)

    const postQuery = useQuery({
        queryKey: [
            'posts',
            'detail',
            parsedPostId,
        ],
        queryFn: ({signal}) =>
            getPost(parsedPostId, signal),
        enabled: isValidPostId,
        retry: false,
    })

    const analysisQuery = useQuery({
        queryKey: [
            'posts',
            'analysis',
            parsedPostId,
        ],
        queryFn: ({signal}) =>
            getFactCheckAnalysis(
                parsedPostId,
                signal,
            ),
        enabled:
            isValidPostId
            && postQuery.isSuccess,
        retry: false,
    })


    const analysisHistoryQuery = useQuery({
        queryKey: [
            'posts',
            'analyses',
            parsedPostId,
        ],
        queryFn: ({signal}) =>
            getFactCheckHistory(
                parsedPostId,
                signal,
            ),
        enabled:
            isValidPostId
            && postQuery.isSuccess,
        retry: false,
    })

    const analysisDetailQuery = useQuery({
        queryKey: [
            'posts',
            'analyses',
            parsedPostId,
            'detail',
            viewedAnalysisId,
        ],
        queryFn: ({signal}) =>
            getFactCheckDetail(
                parsedPostId,
                viewedAnalysisId as number,
                signal,
            ),
        enabled:
            isValidPostId
            && viewedAnalysisId !== null,
        retry: false,
    })

    const runAnalysisMutation =
        useMutation({
            mutationFn: () =>
                runFactCheckAnalysis(
                    parsedPostId,
                ),

            onSuccess: (analysis) => {
                setViewedAnalysisId(null)

                queryClient.setQueryData(
                    [
                        'posts',
                        'analysis',
                        parsedPostId,
                    ],
                    analysis,
                )

                void queryClient.invalidateQueries({
                    queryKey: [
                        'posts',
                        'analyses',
                        parsedPostId,
                    ],
                })

                void queryClient.invalidateQueries({
                    queryKey: ['posts'],
                })

                window.setTimeout(() => {
                    document
                        .getElementById(
                            'fact-check-result',
                        )
                        ?.scrollIntoView({
                            behavior: 'smooth',
                            block: 'start',
                        })
                }, 50)
            },
        })

    const selectRepresentativeMutation =
        useMutation({
            mutationFn: (analysisId: number) =>
                changeRepresentativeAnalysis(
                    parsedPostId,
                    analysisId,
                ),

            onSuccess: (analysis) => {
                setViewedAnalysisId(null)

                queryClient.setQueryData(
                    [
                        'posts',
                        'analysis',
                        parsedPostId,
                    ],
                    analysis,
                )

                void queryClient.invalidateQueries({
                    queryKey: [
                        'posts',
                        'analyses',
                        parsedPostId,
                    ],
                })

                void queryClient.invalidateQueries({
                    queryKey: ['posts'],
                })
            },
        })

    const deleteMutation = useMutation({
        mutationFn: () =>
            deletePost(parsedPostId),

        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: ['posts'],
            })

            queryClient.removeQueries({
                queryKey: [
                    'posts',
                    'detail',
                    parsedPostId,
                ],
            })

            queryClient.removeQueries({
                queryKey: [
                    'posts',
                    'analysis',
                    parsedPostId,
                ],
            })

            navigate('/', {
                replace: true,
            })
        },
    })

    const handleCopyLink = async () => {
        try {
            await navigator.clipboard.writeText(
                window.location.href,
            )

            setIsCopied(true)

            window.setTimeout(
                () => setIsCopied(false),
                1800,
            )
        } catch {
            setIsCopied(false)
        }
    }

    const handleRunAnalysis = () => {
        if (analysisQuery.data) {
            const confirmed = window.confirm(
                '기존 분석 결과를 유지한 상태로 새로운 분석을 실행합니다. 계속할까요?',
            )

            if (!confirmed) {
                return
            }
        }

        runAnalysisMutation.mutate()
    }


    const handleViewAnalysis = (
        item: FactCheckHistoryItem,
    ) => {
        if (item.status !== 'COMPLETED') {
            return
        }

        const representativeId =
            analysisQuery.data?.analysisId
            ?? null

        setViewedAnalysisId(
            item.analysisId === representativeId
                ? null
                : item.analysisId,
        )

        window.setTimeout(() => {
            document
                .getElementById(
                    'fact-check-result',
                )
                ?.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start',
                })
        }, 50)
    }

    const handleSelectRepresentative = (
        analysisId: number,
    ) => {
        const confirmed = window.confirm(
            '이 분석 결과를 게시글의 대표 분석으로 지정할까요?',
        )
        if (!confirmed) {
            return
        }

        selectRepresentativeMutation.mutate(
            analysisId,
        )
    }

    if (!isValidPostId) {
        return (
            <section className="post-detail-page">
                <div className="container post-detail-state-card">
                    <ShieldCheck size={34}/>

                    <h1>
                        잘못된 게시글 주소입니다.
                    </h1>

                    <p>
                        게시글 번호를 확인한 뒤 다시
                        접근해 주세요.
                    </p>

                    <Link to="/">
                        홈으로 돌아가기
                    </Link>
                </div>
            </section>
        )
    }

    if (postQuery.isPending) {
        return <PostDetailSkeleton/>
    }

    if (postQuery.isError) {
        return (
            <section className="post-detail-page">
                <div className="container post-detail-state-card">
                    <ShieldCheck size={34}/>

                    <h1>
                        게시글을 표시할 수 없습니다.
                    </h1>

                    <p>
                        {getPostErrorMessage(
                            postQuery.error,
                        )}
                    </p>

                    <div className="post-detail-state-actions">
                        <Button
                            type="button"
                            variant="primary"
                            onClick={() =>
                                void postQuery.refetch()
                            }
                        >
                            <RotateCcw size={17}/>
                            다시 시도
                        </Button>

                        <Link to="/">
                            홈으로 돌아가기
                        </Link>
                    </div>
                </div>
            </section>
        )
    }

    const post = postQuery.data
    const user = session.data
    const analysis = analysisQuery.data
    const analysisHistory =
        analysisHistoryQuery.data ?? []

    const displayedAnalysis =
        viewedAnalysisId === null
            ? analysis
            : analysisDetailQuery.data

    const wasUpdated =
        post.updatedAt
        && post.updatedAt !== post.createdAt

    const isOwner =
        user?.userId === post.authorId

    const isAdmin =
        user?.role === 'ADMIN'

    const canRunAnalysis =
        isOwner || isAdmin

    const verdict =
        getVerdictPresentation(
            analysis?.verdict ?? null,
        )

    return (
        <section className="post-detail-page">
            <div className="container">
                <Link
                    to="/"
                    className="post-detail-back-link"
                >
                    <ArrowLeft size={17}/>
                    홈으로 돌아가기
                </Link>

                <div className="post-detail-grid">
                    <div className="post-detail-main-column">
                        <article className="post-detail-card">
                            <header className="post-detail-header">
                                <div className="post-detail-header__top">
                                    <Badge tone="brand">
                                        {post.category}
                                    </Badge>

                                    <div className="post-detail-header-actions">
                    <span className="post-detail-status">
                      <CheckCircle2
                          size={15}
                      />
                      공개 게시글
                    </span>

                                        {isOwner && (
                                            <div className="post-detail-owner-actions">
                                                <Link
                                                    to={`/posts/${post.postId}/edit`}
                                                    className="post-detail-owner-button"
                                                >
                                                    <Pencil size={15}/>
                                                    수정
                                                </Link>

                                                <button
                                                    type="button"
                                                    className="post-detail-owner-button post-detail-owner-button--danger"
                                                    onClick={() =>
                                                        setIsDeleteDialogOpen(
                                                            true,
                                                        )
                                                    }
                                                >
                                                    <Trash2 size={15}/>
                                                    삭제
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                </div>

                                <h1>{post.title}</h1>

                                <div className="post-detail-author-row">
                  <span className="post-detail-avatar">
                    {post.authorNickname
                        .slice(0, 1)
                        .toUpperCase()}
                  </span>

                                    <div>
                                        <strong>
                                            {post.authorNickname}
                                        </strong>
                                        <span>작성자</span>
                                    </div>
                                </div>

                                <div className="post-detail-meta">
                  <span>
                    <CalendarDays
                        size={15}
                    />
                      {formatDate(
                          post.createdAt,
                      )}
                  </span>

                                    <span>
                    <Eye size={15}/>
                    조회{' '}
                                        {post.viewCount.toLocaleString(
                                            'ko-KR',
                                        )}
                  </span>
                                    <span className="post-detail-like-count">
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

                                    {wasUpdated && (
                                        <span>
                      수정{' '}
                                            {formatDate(
                                                post.updatedAt,
                                            )}
                    </span>
                                    )}
                                </div>
                            </header>

                            <div className="post-detail-divider"/>

                            <div className="post-detail-content">
                                {post.content}
                            </div>

                            <footer className="post-detail-footer">
                                <p>
                                    이 글의 주장은 출처와
                                    근거를 함께 확인하는 것이
                                    좋습니다.
                                </p>

                                <button
                                    type="button"
                                    onClick={handleCopyLink}
                                >
                                    {isCopied ? (
                                        <CheckCircle2
                                            size={17}
                                        />
                                    ) : (
                                        <Copy size={17}/>
                                    )}

                                    {isCopied
                                        ? '주소 복사 완료'
                                        : '게시글 주소 복사'}
                                </button>
                            </footer>
                        </article>

                        <PostSharePanel title={post.title}/>

                        {viewedAnalysisId !== null
                            && analysisDetailQuery.isPending && (
                                <div className="fact-check-detail-loading">
                                    <LoaderCircle
                                        size={23}
                                        className="spin"
                                    />
                                    과거 분석 결과를 불러오고 있습니다.
                                </div>
                            )}

                        {analysisDetailQuery.isError && (
                            <div className="fact-check-detail-loading fact-check-detail-loading--error">
                                <Info size={21}/>
                                {getFactCheckErrorMessage(
                                    analysisDetailQuery.error,
                                )}
                            </div>
                        )}

                        {displayedAnalysis?.status
                            === 'COMPLETED' && (
                                <FactCheckResultSection
                                    analysis={displayedAnalysis}
                                />
                            )}

                        <FactCheckHistorySection
                            history={analysisHistory}
                            representativeAnalysisId={
                                analysis?.analysisId
                                ?? null
                            }
                            viewedAnalysisId={
                                viewedAnalysisId
                            }
                            canManage={canRunAnalysis}
                            isLoading={
                                analysisHistoryQuery.isPending
                            }
                            isError={
                                analysisHistoryQuery.isError
                            }
                            error={
                                analysisHistoryQuery.error
                            }
                            isSelecting={
                                selectRepresentativeMutation.isPending
                            }
                            onRetry={() =>
                                void analysisHistoryQuery.refetch()
                            }
                            onView={handleViewAnalysis}
                            onSelectRepresentative={
                                handleSelectRepresentative
                            }
                        />

                        <PostInteractions
                            postId={post.postId}
                        />
                    </div>

                    <aside className="post-detail-sidebar">
                        <section className="post-detail-side-card post-detail-analysis-card">
              <span className="post-detail-side-icon">
                <Sparkles size={22}/>
              </span>

                            <Badge
                                tone={
                                    analysis
                                        ? verdict.tone
                                        : 'brand'
                                }
                            >
                                {analysis
                                    ? verdict.label
                                    : 'AI 팩트체크'}
                            </Badge>

                            {analysisQuery.isPending ? (
                                <div className="fact-check-sidebar-loading">
                                    <LoaderCircle
                                        size={23}
                                        className="spin"
                                    />

                                    <strong>
                                        분석 결과 확인 중
                                    </strong>

                                    <span>
                    저장된 팩트체크 결과를
                    불러오고 있습니다.
                  </span>
                                </div>
                            ) : analysisQuery.isError ? (
                                <div className="fact-check-sidebar-error">
                                    <Info size={19}/>

                                    <strong>
                                        분석 결과를 불러오지
                                        못했습니다.
                                    </strong>

                                    <span>
                    {getFactCheckErrorMessage(
                        analysisQuery.error,
                    )}
                  </span>

                                    <button
                                        type="button"
                                        onClick={() =>
                                            void analysisQuery.refetch()
                                        }
                                    >
                                        <RefreshCw size={15}/>
                                        다시 시도
                                    </button>
                                </div>
                            ) : (
                                <>
                                    <h2>
                                        {analysis
                                            ? 'AI 분석이 완료되었습니다.'
                                            : '이 게시글의 주장을 검증해보세요.'}
                                    </h2>

                                    <p>
                                        {analysis
                                            ? analysis.summary
                                            || verdict.description
                                            : '핵심 주장을 분리하고 신뢰할 수 있는 출처를 연결해 판단에 필요한 맥락을 제공합니다.'}
                                    </p>

                                    {!analysis && (
                                        <div className="post-detail-analysis-steps">
                      <span>
                        <CheckCircle2
                            size={15}
                        />
                        핵심 주장 추출
                      </span>

                                            <span>
                        <CheckCircle2
                            size={15}
                        />
                        출처 기반 검증
                      </span>

                                            <span>
                        <CheckCircle2
                            size={15}
                        />
                        결과와 근거 공개
                      </span>
                                        </div>
                                    )}

                                    {runAnalysisMutation.isPending && (
                                        <div className="fact-check-running">
                      <span className="fact-check-running__icon">
                        <LoaderCircle
                            size={21}
                            className="spin"
                        />
                      </span>

                                            <div>
                                                <strong>
                                                    Gemini가 분석 중입니다.
                                                </strong>

                                                <span>
                          검색과 출처 검증으로
                          시간이 조금 걸릴 수
                          있습니다.
                        </span>
                                            </div>
                                        </div>
                                    )}

                                    {runAnalysisMutation.isError && (
                                        <div className="fact-check-run-error">
                                            <Info size={17}/>

                                            <span>
                        {getFactCheckErrorMessage(
                            runAnalysisMutation.error,
                        )}
                      </span>
                                        </div>
                                    )}


                                    {selectRepresentativeMutation.isError && (
                                        <div className="fact-check-run-error">
                                            <Info size={17}/>

                                            <span>
                                                {getFactCheckErrorMessage(
                                                    selectRepresentativeMutation.error,
                                                )}
                                            </span>
                                        </div>
                                    )}

                                    <div className="fact-check-sidebar-actions">
                                        {analysis && (
                                            <a
                                                href="#fact-check-result"
                                                className="fact-check-result-link"
                                            >
                                                <Gauge size={16}/>
                                                결과 자세히 보기
                                            </a>
                                        )}

                                        {canRunAnalysis ? (
                                            <button
                                                type="button"
                                                className="fact-check-run-button"
                                                onClick={
                                                    handleRunAnalysis
                                                }
                                                disabled={
                                                    runAnalysisMutation.isPending
                                                }
                                            >
                                                {runAnalysisMutation.isPending ? (
                                                    <>
                                                        <LoaderCircle
                                                            size={17}
                                                            className="spin"
                                                        />
                                                        분석 중
                                                    </>
                                                ) : analysis ? (
                                                    <>
                                                        <RefreshCw
                                                            size={17}
                                                        />
                                                        다시 분석하기
                                                    </>
                                                ) : (
                                                    <>
                                                        <ShieldCheck
                                                            size={17}
                                                        />
                                                        AI 팩트체크 시작
                                                    </>
                                                )}
                                            </button>
                                        ) : !user ? (
                                            <Link
                                                to="/login"
                                                className="fact-check-login-link"
                                            >
                                                작성자 계정으로 로그인
                                            </Link>
                                        ) : (
                                            <button
                                                type="button"
                                                className="fact-check-run-button"
                                                disabled
                                            >
                                                작성자만 분석 가능
                                            </button>
                                        )}
                                    </div>
                                </>
                            )}
                        </section>

                        <section className="post-detail-side-card post-detail-guide-card">
                            <div className="post-detail-guide-card__title">
                                <UserRound size={19}/>

                                <h2>
                                    읽을 때 확인할 점
                                </h2>
                            </div>

                            <ul>
                                <li>
                                    주장의 원문 출처가
                                    제시되어 있는지
                                </li>
                                <li>
                                    수치와 날짜가 최신
                                    정보인지
                                </li>
                                <li>
                                    사실과 작성자의 의견이
                                    구분되어 있는지
                                </li>
                            </ul>
                        </section>

                        <div className="post-detail-quick-actions">
                            <button
                                type="button"
                                onClick={handleCopyLink}
                            >
                                <Share2 size={17}/>
                                공유
                            </button>

                            <button
                                type="button"
                                disabled
                            >
                                <Bookmark size={17}/>
                                저장
                            </button>
                        </div>
                    </aside>
                </div>
            </div>

            {isDeleteDialogOpen && (
                <div
                    className="post-delete-dialog-backdrop"
                    role="presentation"
                    onMouseDown={(event) => {
                        if (
                            event.target
                            === event.currentTarget
                            && !deleteMutation.isPending
                        ) {
                            setIsDeleteDialogOpen(false)
                        }
                    }}
                >
                    <div
                        className="post-delete-dialog"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="delete-dialog-title"
                    >
                        <button
                            type="button"
                            className="post-delete-dialog__close"
                            aria-label="삭제 창 닫기"
                            onClick={() =>
                                setIsDeleteDialogOpen(false)
                            }
                            disabled={
                                deleteMutation.isPending
                            }
                        >
                            <X size={19}/>
                        </button>

                        <span className="post-delete-dialog__icon">
              <Trash2 size={24}/>
            </span>

                        <h2 id="delete-dialog-title">
                            게시글을 삭제할까요?
                        </h2>

                        <p>
                            삭제된 게시글은 홈과 상세
                            페이지에서 더 이상 확인할 수
                            없습니다.
                        </p>

                        {deleteMutation.isError && (
                            <div className="post-delete-dialog__error">
                                {getPostErrorMessage(
                                    deleteMutation.error,
                                )}
                            </div>
                        )}

                        <div className="post-delete-dialog__actions">
                            <button
                                type="button"
                                className="post-delete-dialog__cancel"
                                onClick={() =>
                                    setIsDeleteDialogOpen(false)
                                }
                                disabled={
                                    deleteMutation.isPending
                                }
                            >
                                취소
                            </button>

                            <button
                                type="button"
                                className="post-delete-dialog__confirm"
                                onClick={() =>
                                    deleteMutation.mutate()
                                }
                                disabled={
                                    deleteMutation.isPending
                                }
                            >
                                {deleteMutation.isPending ? (
                                    <>
                                        <LoaderCircle
                                            size={17}
                                            className="spin"
                                        />
                                        삭제 중
                                    </>
                                ) : (
                                    <>
                                        <Trash2 size={17}/>
                                        삭제하기
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </section>
    )}
