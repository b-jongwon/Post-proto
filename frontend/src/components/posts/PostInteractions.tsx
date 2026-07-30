import {
    type FormEvent,
    useMemo,
    useState,
} from 'react'
import {
    useInfiniteQuery,
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query'
import {
    Check,
    Heart,
    LoaderCircle,
    LogIn,
    MessageCircle,
    Pencil,
    RefreshCw,
    Send,
    Trash2,
    X,
} from 'lucide-react'



import {
    Link,
    useLocation,
    useNavigate,
} from 'react-router-dom'
import { useSession } from '@/features/auth/hooks/useAuth.ts'
import {
    createComment,
    deleteComment,
    getCommentErrorMessage,
    getComments,
    updateComment,
} from '@/features/comments/commentApi.ts'
import type { CommentItem } from '@/features/comments/commentTypes.ts'
import {
    getPostLikeErrorMessage,
    getPostLikeStatus,
    likePost,
    unlikePost,
} from '@/features/postLikes/postLikeApi.ts'
import type { PostLikeResponse } from '@/features/postLikes/postLikeTypes.ts'
import type {
    PageResponse,
    PostDetail,
    PostSummary,
} from '@/features/posts/postTypes.ts'
import '@/styles/post-interactions.css'

const COMMENT_PAGE_SIZE = 20
const COMMENT_MAX_LENGTH = 1000

function formatCommentDate(value: string): string {
    const date = new Date(value)

    if (Number.isNaN(date.getTime())) {
        return value
    }

    const diff = Date.now() - date.getTime()

    if (diff >= 0) {
        const minutes = Math.floor(diff / 60_000)

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
    }

    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    }).format(date)
}

function isCommentEdited(comment: CommentItem): boolean {
    return comment.updatedAt !== comment.createdAt
}

interface PostInteractionsProps {
    postId: number
}

export function PostInteractions({
                                     postId,
                                 }: PostInteractionsProps) {
    const session = useSession()
    const user = session.data
    const navigate = useNavigate()
    const location = useLocation()
    const queryClient = useQueryClient()

    const [commentContent, setCommentContent] =
        useState('')
    const [editingCommentId, setEditingCommentId] =
        useState<number | null>(null)
    const [editingContent, setEditingContent] =
        useState('')
    const [deletingCommentId, setDeletingCommentId] =
        useState<number | null>(null)

    const likeQueryKey = [
        'posts',
        'likes',
        postId,
        user?.userId ?? 'guest',
    ] as const

    const commentsQueryKey = [
        'posts',
        'comments',
        postId,
    ] as const

    const likeQuery = useQuery({
        queryKey: likeQueryKey,
        queryFn: ({ signal }) =>
            getPostLikeStatus(postId, signal),
        retry: false,
    })

    const commentsQuery = useInfiniteQuery({
        queryKey: commentsQueryKey,
        queryFn: ({ pageParam, signal }) =>
            getComments(
                postId,
                pageParam,
                COMMENT_PAGE_SIZE,
                signal,
            ),
        initialPageParam: 0,
        getNextPageParam: (lastPage) =>
            lastPage.last
                ? undefined
                : lastPage.page + 1,
        retry: false,
    })

    const comments = useMemo(
        () =>
            commentsQuery.data?.pages.flatMap(
                (page) => page.content,
            ) ?? [],
        [commentsQuery.data],
    )

    const commentCount =
        commentsQuery.data?.pages[0]
            ?.totalElements ?? 0

    const likeMutation = useMutation({
        mutationFn: (currentlyLiked: boolean) =>
            currentlyLiked
                ? unlikePost(postId)
                : likePost(postId),

        onSuccess: (response) => {
            // 하단 좋아요 버튼 상태와 개수 갱신
            queryClient.setQueryData<PostLikeResponse>(
                likeQueryKey,
                response,
            )

            // 게시글 상세페이지 상단 좋아요 개수 갱신
            queryClient.setQueryData<PostDetail>(
                [
                    'posts',
                    'detail',
                    postId,
                ],
                (previous) =>
                    previous
                        ? {
                            ...previous,
                            likeCount: response.likeCount,
                        }
                        : previous,
            )

            // 홈 게시글 카드의 좋아요 개수 갱신
            queryClient.setQueriesData<
                PageResponse<PostSummary>
            >(
                {
                    queryKey: [
                        'posts',
                        'home',
                    ],
                },
                (previous) =>
                    previous
                        ? {
                            ...previous,
                            content: previous.content.map(
                                (post) =>
                                    post.postId === postId
                                        ? {
                                            ...post,
                                            likeCount:
                                            response.likeCount,
                                        }
                                        : post,
                            ),
                        }
                        : previous,
            )

            void queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'highlights',
                ],
            })

            void queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'statistics',
                ],
            })
        },
    })

    const createMutation = useMutation({
        mutationFn: (content: string) =>
            createComment(postId, {
                content,
            }),
        onSuccess: async () => {
            setCommentContent('')

            await queryClient.invalidateQueries({
                queryKey: commentsQueryKey,
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'detail',
                    postId,
                ],
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'home',
                ],
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'highlights',
                ],
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'statistics',
                ],
            })
        },
    })

    const updateMutation = useMutation({
        mutationFn: ({
                         commentId,
                         content,
                     }: {
            commentId: number
            content: string
        }) =>
            updateComment(
                postId,
                commentId,
                {
                    content,
                },
            ),
        onSuccess: async () => {
            setEditingCommentId(null)
            setEditingContent('')

            await queryClient.invalidateQueries({
                queryKey: commentsQueryKey,
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'detail',
                    postId,
                ],
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'home',
                ],
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'highlights',
                ],
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'statistics',
                ],
            })
        },
    })

    const deleteMutation = useMutation({
        mutationFn: (commentId: number) =>
            deleteComment(postId, commentId),
        onSuccess: async () => {
            setDeletingCommentId(null)

            await queryClient.invalidateQueries({
                queryKey: commentsQueryKey,
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'detail',
                    postId,
                ],
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'home',
                ],
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'highlights',
                ],
            })

            await queryClient.invalidateQueries({
                queryKey: [
                    'posts',
                    'statistics',
                ],
            })
        },
        onError: () => {
            setDeletingCommentId(null)
        },
    })

    const goToLogin = () => {
        navigate('/login', {
            state: {
                from: `${location.pathname}${location.search}${location.hash}`,
            },
        })
    }

    const handleLike = () => {
        if (!user) {
            goToLogin()
            return
        }

        if (likeMutation.isPending) {
            return
        }

        likeMutation.mutate(
            likeQuery.data?.liked ?? false,
        )
    }

    const handleCreateComment = (
        event: FormEvent<HTMLFormElement>,
    ) => {
        event.preventDefault()

        if (!user) {
            goToLogin()
            return
        }

        const content = commentContent.trim()

        if (!content || createMutation.isPending) {
            return
        }

        createMutation.mutate(content)
    }

    const startEditing = (comment: CommentItem) => {
        setEditingCommentId(comment.commentId)
        setEditingContent(comment.content)
        updateMutation.reset()
    }

    const cancelEditing = () => {
        setEditingCommentId(null)
        setEditingContent('')
        updateMutation.reset()
    }

    const handleUpdateComment = (
        event: FormEvent<HTMLFormElement>,
        commentId: number,
    ) => {
        event.preventDefault()

        const content = editingContent.trim()

        if (!content || updateMutation.isPending) {
            return
        }

        updateMutation.mutate({
            commentId,
            content,
        })
    }

    const handleDeleteComment = (
        commentId: number,
    ) => {
        const confirmed = window.confirm(
            '이 댓글을 삭제할까요?',
        )

        if (!confirmed) {
            return
        }

        setDeletingCommentId(commentId)
        deleteMutation.mutate(commentId)
    }

    const likeStatus = likeQuery.data
    const isLiked = likeStatus?.liked ?? false
    const likeCount = likeStatus?.likeCount ?? 0

    return (
        <section
            className="post-interactions"
            aria-labelledby="post-interactions-title"
        >
            <header className="post-interactions__header">
                <div>
                    <span className="post-interactions__eyebrow">
                        COMMUNITY
                    </span>
                    <h2 id="post-interactions-title">
                        의견과 반응
                    </h2>
                    <p>
                        게시글에 공감하거나 검증에 필요한
                        의견을 남겨보세요.
                    </p>
                </div>

                <button
                    type="button"
                    className={[
                        'post-like-button',
                        isLiked
                            ? 'post-like-button--active'
                            : '',
                    ]
                        .filter(Boolean)
                        .join(' ')}
                    onClick={handleLike}
                    disabled={
                        likeQuery.isPending
                        || likeMutation.isPending
                    }
                    aria-pressed={isLiked}
                >
                    {likeMutation.isPending ? (
                        <LoaderCircle
                            size={19}
                            className="spin"
                        />
                    ) : (
                        <Heart
                            size={19}
                            fill={
                                isLiked
                                    ? 'currentColor'
                                    : 'none'
                            }
                        />
                    )}

                    <span>
                        {isLiked ? '좋아요 취소' : '좋아요'}
                    </span>

                    <strong>
                        {likeCount.toLocaleString('ko-KR')}
                    </strong>
                </button>
            </header>

            {(likeQuery.isError
                || likeMutation.isError) && (
                <div className="post-interactions__error">
                    {getPostLikeErrorMessage(
                        likeMutation.error
                        ?? likeQuery.error,
                    )}
                </div>
            )}

            <div className="post-comments">
                <div className="post-comments__heading">
                    <div>
                        <MessageCircle size={20} />
                        <h3>댓글</h3>
                        <span>
                            {commentCount.toLocaleString(
                                'ko-KR',
                            )}
                        </span>
                    </div>

                    <button
                        type="button"
                        onClick={() =>
                            void commentsQuery.refetch()
                        }
                        disabled={commentsQuery.isFetching}
                        aria-label="댓글 새로고침"
                    >
                        <RefreshCw
                            size={16}
                            className={
                                commentsQuery.isFetching
                                    ? 'spin'
                                    : undefined
                            }
                        />
                    </button>
                </div>

                {user ? (
                    <form
                        className="comment-composer"
                        onSubmit={handleCreateComment}
                    >
                        <span className="comment-avatar">
                            {user.nickname
                                .slice(0, 1)
                                .toUpperCase()}
                        </span>

                        <div className="comment-composer__body">
                            <textarea
                                value={commentContent}
                                onChange={(event) =>
                                    setCommentContent(
                                        event.target.value,
                                    )
                                }
                                maxLength={COMMENT_MAX_LENGTH}
                                placeholder="검증에 도움이 되는 의견을 남겨보세요."
                                aria-label="댓글 내용"
                            />

                            <div className="comment-composer__footer">
                                <span>
                                    {commentContent.length}
                                    {' / '}
                                    {COMMENT_MAX_LENGTH}
                                </span>

                                <button
                                    type="submit"
                                    disabled={
                                        !commentContent.trim()
                                        || createMutation.isPending
                                    }
                                >
                                    {createMutation.isPending ? (
                                        <LoaderCircle
                                            size={16}
                                            className="spin"
                                        />
                                    ) : (
                                        <Send size={16} />
                                    )}
                                    댓글 등록
                                </button>
                            </div>
                        </div>
                    </form>
                ) : (
                    <div className="comment-login-callout">
                        <LogIn size={20} />
                        <div>
                            <strong>
                                로그인 후 댓글을 작성할 수
                                있습니다.
                            </strong>
                            <span>
                                의견을 남기고 다른 사용자와
                                검증 근거를 공유해 보세요.
                            </span>
                        </div>
                        <Link
                            to="/login"
                            state={{
                                from: `${location.pathname}${location.search}${location.hash}`,
                            }}
                        >
                            로그인
                        </Link>
                    </div>
                )}

                {createMutation.isError && (
                    <div className="post-interactions__error">
                        {getCommentErrorMessage(
                            createMutation.error,
                        )}
                    </div>
                )}

                {commentsQuery.isPending ? (
                    <div className="comments-state">
                        <LoaderCircle
                            size={22}
                            className="spin"
                        />
                        댓글을 불러오고 있습니다.
                    </div>
                ) : commentsQuery.isError ? (
                    <div className="comments-state comments-state--error">
                        <MessageCircle size={22} />
                        <strong>
                            댓글을 불러오지 못했습니다.
                        </strong>
                        <span>
                            {getCommentErrorMessage(
                                commentsQuery.error,
                            )}
                        </span>
                        <button
                            type="button"
                            onClick={() =>
                                void commentsQuery.refetch()
                            }
                        >
                            다시 시도
                        </button>
                    </div>
                ) : comments.length === 0 ? (
                    <div className="comments-state">
                        <MessageCircle size={24} />
                        <strong>아직 댓글이 없습니다.</strong>
                        <span>
                            첫 번째 의견을 남겨보세요.
                        </span>
                    </div>
                ) : (
                    <ol className="comment-list">
                        {comments.map((comment) => {
                            const isOwner =
                                user?.userId
                                === comment.authorId
                            const isEditing =
                                editingCommentId
                                === comment.commentId
                            const isDeleting =
                                deletingCommentId
                                === comment.commentId

                            return (
                                <li
                                    key={comment.commentId}
                                    className="comment-item"
                                >
                                    <span className="comment-avatar">
                                        {comment.authorNickname
                                            .slice(0, 1)
                                            .toUpperCase()}
                                    </span>

                                    <div className="comment-item__body">
                                        <div className="comment-item__top">
                                            <div>
                                                <strong>
                                                    {comment.authorNickname}
                                                </strong>
                                                <span>
                                                    {formatCommentDate(
                                                        comment.createdAt,
                                                    )}
                                                    {isCommentEdited(
                                                        comment,
                                                    )
                                                        ? ' · 수정됨'
                                                        : ''}
                                                </span>
                                            </div>

                                            {isOwner && !isEditing && (
                                                <div className="comment-item__actions">
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            startEditing(
                                                                comment,
                                                            )
                                                        }
                                                        aria-label="댓글 수정"
                                                    >
                                                        <Pencil
                                                            size={15}
                                                        />
                                                    </button>

                                                    <button
                                                        type="button"
                                                        className="comment-item__delete"
                                                        onClick={() =>
                                                            handleDeleteComment(
                                                                comment.commentId,
                                                            )
                                                        }
                                                        disabled={isDeleting}
                                                        aria-label="댓글 삭제"
                                                    >
                                                        {isDeleting ? (
                                                            <LoaderCircle
                                                                size={15}
                                                                className="spin"
                                                            />
                                                        ) : (
                                                            <Trash2
                                                                size={15}
                                                            />
                                                        )}
                                                    </button>
                                                </div>
                                            )}
                                        </div>

                                        {isEditing ? (
                                            <form
                                                className="comment-edit-form"
                                                onSubmit={(event) =>
                                                    handleUpdateComment(
                                                        event,
                                                        comment.commentId,
                                                    )
                                                }
                                            >
                                                <textarea
                                                    value={editingContent}
                                                    onChange={(event) =>
                                                        setEditingContent(
                                                            event.target.value,
                                                        )
                                                    }
                                                    maxLength={
                                                        COMMENT_MAX_LENGTH
                                                    }
                                                    autoFocus
                                                    aria-label="수정할 댓글 내용"
                                                />

                                                <div className="comment-edit-form__footer">
                                                    <span>
                                                        {editingContent.length}
                                                        {' / '}
                                                        {COMMENT_MAX_LENGTH}
                                                    </span>

                                                    <div>
                                                        <button
                                                            type="button"
                                                            onClick={cancelEditing}
                                                            disabled={
                                                                updateMutation.isPending
                                                            }
                                                        >
                                                            <X size={15} />
                                                            취소
                                                        </button>

                                                        <button
                                                            type="submit"
                                                            className="comment-edit-form__save"
                                                            disabled={
                                                                !editingContent.trim()
                                                                || updateMutation.isPending
                                                            }
                                                        >
                                                            {updateMutation.isPending ? (
                                                                <LoaderCircle
                                                                    size={15}
                                                                    className="spin"
                                                                />
                                                            ) : (
                                                                <Check
                                                                    size={15}
                                                                />
                                                            )}
                                                            저장
                                                        </button>
                                                    </div>
                                                </div>

                                                {updateMutation.isError && (
                                                    <div className="post-interactions__error">
                                                        {getCommentErrorMessage(
                                                            updateMutation.error,
                                                        )}
                                                    </div>
                                                )}
                                            </form>
                                        ) : (
                                            <p>{comment.content}</p>
                                        )}
                                    </div>
                                </li>
                            )
                        })}
                    </ol>
                )}

                {deleteMutation.isError && (
                    <div className="post-interactions__error">
                        {getCommentErrorMessage(
                            deleteMutation.error,
                        )}
                    </div>
                )}

                {commentsQuery.hasNextPage && (
                    <button
                        type="button"
                        className="comments-load-more"
                        onClick={() =>
                            void commentsQuery.fetchNextPage()
                        }
                        disabled={
                            commentsQuery.isFetchingNextPage
                        }
                    >
                        {commentsQuery.isFetchingNextPage && (
                            <LoaderCircle
                                size={16}
                                className="spin"
                            />
                        )}
                        댓글 더 보기
                    </button>
                )}
            </div>
        </section>
    )
}
