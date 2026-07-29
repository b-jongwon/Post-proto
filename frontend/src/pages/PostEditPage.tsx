import {
  ArrowLeft,
  CheckCircle2,
  FileText,
  Info,
  LoaderCircle,
  PencilLine,
  Save,
  ShieldCheck,
  Sparkles,
  Tag,
  Type,
} from 'lucide-react'
import {
  type FormEvent,
  useMemo,
  useState,
} from 'react'
import {
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import {
  Link,
  useNavigate,
  useParams,
} from 'react-router-dom'
import { ApiError } from '@/api/ApiError'
import { useSession } from '@/features/auth/hooks/useAuth'
import {
  getPost,
  getPostErrorMessage,
  updatePost,
} from '@/features/posts/postApi'
import type {
  PostDetail,
  PostUpdateRequest,
} from '@/features/posts/postTypes'
import '@/styles/post-editor.css'

const TITLE_MAX_LENGTH = 200
const CONTENT_MAX_LENGTH = 20_000
const CATEGORY_MAX_LENGTH = 50

const categories = [
  'AI',
  '정치',
  '경제',
  '사회',
  '과학',
  '기술',
  '건강',
  '생활',
  '자동차',
  '기타',
]

type FieldErrors = Partial<
  Record<keyof PostUpdateRequest, string>
>

function validateForm(
  form: PostUpdateRequest,
): FieldErrors {
  const errors: FieldErrors = {}

  if (!form.title.trim()) {
    errors.title = '제목을 입력해 주세요.'
  } else if (
    form.title.length > TITLE_MAX_LENGTH
  ) {
    errors.title =
      `제목은 ${TITLE_MAX_LENGTH}자 이하여야 합니다.`
  }

  if (!form.category.trim()) {
    errors.category =
      '카테고리를 선택해 주세요.'
  } else if (
    form.category.length
    > CATEGORY_MAX_LENGTH
  ) {
    errors.category =
      `카테고리는 ${CATEGORY_MAX_LENGTH}자 이하여야 합니다.`
  }

  if (!form.content.trim()) {
    errors.content =
      '검증할 내용을 입력해 주세요.'
  } else if (
    form.content.length
    > CONTENT_MAX_LENGTH
  ) {
    errors.content =
      `본문은 ${CONTENT_MAX_LENGTH.toLocaleString()}자 이하여야 합니다.`
  }

  return errors
}

function PostEditForm({
  post,
}: {
  post: PostDetail
}) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const initialForm = useMemo<PostUpdateRequest>(
    () => ({
      title: post.title,
      content: post.content,
      category: post.category,
    }),
    [
      post.title,
      post.content,
      post.category,
    ],
  )

  const [form, setForm] =
    useState<PostUpdateRequest>(initialForm)

  const [fieldErrors, setFieldErrors] =
    useState<FieldErrors>({})

  const [submitError, setSubmitError] =
    useState('')

  const [isSubmitting, setIsSubmitting] =
    useState(false)

  const isDirty =
    form.title !== initialForm.title
    || form.content !== initialForm.content
    || form.category !== initialForm.category

  const completedFields = [
    Boolean(form.title.trim()),
    Boolean(form.category.trim()),
    Boolean(form.content.trim()),
  ].filter(Boolean).length

  const completionPercent = Math.round(
    (completedFields / 3) * 100,
  )

  const canSubmit =
    isDirty
    && Boolean(form.title.trim())
    && Boolean(form.category.trim())
    && Boolean(form.content.trim())
    && form.title.length <= TITLE_MAX_LENGTH
    && form.category.length
      <= CATEGORY_MAX_LENGTH
    && form.content.length
      <= CONTENT_MAX_LENGTH
    && !isSubmitting

  const updateField = (
    field: keyof PostUpdateRequest,
    value: string,
  ) => {
    setForm((previous) => ({
      ...previous,
      [field]: value,
    }))

    setFieldErrors((previous) => ({
      ...previous,
      [field]: undefined,
    }))

    setSubmitError('')
  }

  const handleCancel = () => {
    if (
      isDirty
      && !window.confirm(
        '수정 중인 내용이 사라집니다. 페이지를 나가시겠어요?',
      )
    ) {
      return
    }

    navigate(`/posts/${post.postId}`)
  }

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>,
  ) => {
    event.preventDefault()

    const validationErrors =
      validateForm(form)

    if (
      Object.keys(validationErrors).length > 0
    ) {
      setFieldErrors(validationErrors)
      return
    }

    setIsSubmitting(true)
    setSubmitError('')

    try {
      const updatedPost = await updatePost(
        post.postId,
        {
          title: form.title.trim(),
          category: form.category.trim(),
          content: form.content.trim(),
        },
      )

      queryClient.setQueryData(
        [
          'posts',
          'detail',
          post.postId,
        ],
        updatedPost,
      )

      await queryClient.invalidateQueries({
        queryKey: ['posts'],
      })

      navigate(
        `/posts/${updatedPost.postId}`,
        {
          replace: true,
        },
      )
    } catch (error) {
      if (error instanceof ApiError) {
        setSubmitError(error.message)

        setFieldErrors({
          title: error.fields.title,
          category: error.fields.category,
          content: error.fields.content,
        })
      } else {
        setSubmitError(
          '게시글을 수정하지 못했습니다. 잠시 후 다시 시도해 주세요.',
        )
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="post-editor-page">
      <div className="post-editor-page__glow post-editor-page__glow--one" />
      <div className="post-editor-page__glow post-editor-page__glow--two" />

      <div className="container post-editor-shell">
        <header className="post-editor-header">
          <button
            type="button"
            className="post-editor-back"
            onClick={handleCancel}
          >
            <ArrowLeft size={18} />
            게시글로 돌아가기
          </button>

          <div className="post-editor-heading">
            <span className="post-editor-heading__eyebrow">
              <PencilLine size={15} />
              게시글 수정
            </span>

            <h1>
              검증 요청을 더 명확하게
              다듬어보세요
            </h1>

            <p>
              제목과 핵심 주장을 구체적으로
              작성하면 이후 AI 분석 결과의
              정확도를 높일 수 있습니다.
            </p>
          </div>
        </header>

        <div className="post-editor-grid">
          <form
            className="post-editor-card"
            onSubmit={handleSubmit}
            noValidate
          >
            <div className="post-editor-card__header">
              <div>
                <span>EDIT POST</span>
                <h2>게시글 내용 수정</h2>
              </div>

              <div className="post-editor-progress">
                <div>
                  <strong>
                    {completionPercent}%
                  </strong>
                  <span>작성 완료</span>
                </div>

                <div className="post-editor-progress__track">
                  <span
                    style={{
                      width:
                        `${completionPercent}%`,
                    }}
                  />
                </div>
              </div>
            </div>

            {submitError && (
              <div
                className="post-editor-alert"
                role="alert"
              >
                <Info size={18} />
                <span>{submitError}</span>
              </div>
            )}

            <div className="post-editor-field">
              <div className="post-editor-label-row">
                <label htmlFor="post-title">
                  <span className="post-editor-label-icon">
                    <Type size={17} />
                  </span>
                  제목
                  <em>필수</em>
                </label>

                <span>
                  {form.title.length}/
                  {TITLE_MAX_LENGTH}
                </span>
              </div>

              <input
                id="post-title"
                type="text"
                value={form.title}
                onChange={(event) =>
                  updateField(
                    'title',
                    event.target.value,
                  )
                }
                maxLength={
                  TITLE_MAX_LENGTH + 1
                }
                aria-invalid={Boolean(
                  fieldErrors.title,
                )}
              />

              {fieldErrors.title && (
                <p className="post-editor-field__error">
                  {fieldErrors.title}
                </p>
              )}
            </div>

            <div className="post-editor-field">
              <div className="post-editor-label-row">
                <label htmlFor="post-category">
                  <span className="post-editor-label-icon">
                    <Tag size={17} />
                  </span>
                  카테고리
                  <em>필수</em>
                </label>
              </div>

              <div className="post-editor-select-wrap">
                <select
                  id="post-category"
                  value={form.category}
                  onChange={(event) =>
                    updateField(
                      'category',
                      event.target.value,
                    )
                  }
                  aria-invalid={Boolean(
                    fieldErrors.category,
                  )}
                >
                  <option value="">
                    카테고리를 선택하세요
                  </option>

                  {categories.map(
                    (category) => (
                      <option
                        key={category}
                        value={category}
                      >
                        {category}
                      </option>
                    ),
                  )}
                </select>
              </div>

              {fieldErrors.category && (
                <p className="post-editor-field__error">
                  {fieldErrors.category}
                </p>
              )}
            </div>

            <div className="post-editor-field post-editor-field--content">
              <div className="post-editor-label-row">
                <label htmlFor="post-content">
                  <span className="post-editor-label-icon">
                    <FileText size={17} />
                  </span>
                  검증할 내용
                  <em>필수</em>
                </label>

                <span>
                  {form.content.length.toLocaleString()}
                  /
                  {CONTENT_MAX_LENGTH.toLocaleString()}
                </span>
              </div>

              <textarea
                id="post-content"
                value={form.content}
                onChange={(event) =>
                  updateField(
                    'content',
                    event.target.value,
                  )
                }
                maxLength={
                  CONTENT_MAX_LENGTH + 1
                }
                aria-invalid={Boolean(
                  fieldErrors.content,
                )}
              />

              {fieldErrors.content && (
                <p className="post-editor-field__error">
                  {fieldErrors.content}
                </p>
              )}
            </div>

            <div className="post-editor-actions">
              <button
                type="button"
                className="post-editor-button post-editor-button--secondary"
                onClick={handleCancel}
                disabled={isSubmitting}
              >
                취소
              </button>

              <button
                type="submit"
                className="post-editor-button post-editor-button--primary"
                disabled={!canSubmit}
              >
                {isSubmitting ? (
                  <>
                    <LoaderCircle
                      size={18}
                      className="is-spinning"
                    />
                    저장 중
                  </>
                ) : (
                  <>
                    수정 내용 저장
                    <Save size={17} />
                  </>
                )}
              </button>
            </div>
          </form>

          <aside className="post-editor-side">
            <section className="post-editor-preview">
              <div className="post-editor-side-heading">
                <span className="post-editor-side-icon">
                  <Sparkles size={19} />
                </span>

                <div>
                  <span>PREVIEW</span>
                  <h2>수정 미리보기</h2>
                </div>
              </div>

              <div className="post-editor-preview-card">
                <span className="post-editor-preview-card__category">
                  {form.category
                    || '카테고리'}
                </span>

                <h3>
                  {form.title.trim()
                    || '게시글 제목'}
                </h3>

                <p>
                  {form.content.trim()
                    ? form.content
                      .trim()
                      .slice(0, 120)
                    : '게시글 내용'}

                  {form.content.trim().length
                    > 120
                    ? '...'
                    : ''}
                </p>

                <div className="post-editor-preview-card__footer">
                  <span className="post-editor-preview-avatar">
                    {post.authorNickname
                      .slice(0, 1)
                      .toUpperCase()}
                  </span>

                  <div>
                    <strong>
                      {post.authorNickname}
                    </strong>
                    <span>수정 중</span>
                  </div>
                </div>
              </div>
            </section>

            <section className="post-editor-guide">
              <div className="post-editor-side-heading">
                <span className="post-editor-side-icon">
                  <ShieldCheck size={19} />
                </span>

                <div>
                  <span>CHECK LIST</span>
                  <h2>저장 전 확인</h2>
                </div>
              </div>

              <ul>
                <li>
                  <CheckCircle2 size={17} />
                  <div>
                    <strong>
                      주장이 명확한가요?
                    </strong>
                    <span>
                      하나의 핵심 내용을
                      중심으로 작성하세요.
                    </span>
                  </div>
                </li>

                <li>
                  <CheckCircle2 size={17} />
                  <div>
                    <strong>
                      수치와 날짜가 정확한가요?
                    </strong>
                    <span>
                      검증에 필요한 구체적인
                      정보를 확인하세요.
                    </span>
                  </div>
                </li>

                <li>
                  <CheckCircle2 size={17} />
                  <div>
                    <strong>
                      기존 분석 결과
                    </strong>
                    <span>
                      내용 수정 후에는 AI
                      분석을 다시 실행하는 것이
                      좋습니다.
                    </span>
                  </div>
                </li>
              </ul>
            </section>
          </aside>
        </div>
      </div>
    </section>
  )
}

export function PostEditPage() {
  const { postId } = useParams()
  const session = useSession()

  const parsedPostId = Number(postId)

  const isValidPostId =
    Number.isInteger(parsedPostId)
    && parsedPostId > 0

  const postQuery = useQuery({
    queryKey: [
      'posts',
      'detail',
      parsedPostId,
    ],
    queryFn: ({ signal }) =>
      getPost(parsedPostId, signal),
    enabled: isValidPostId,
    retry: false,
  })

  if (!isValidPostId) {
    return (
      <section className="post-detail-page">
        <div className="container post-detail-state-card">
          <ShieldCheck size={34} />
          <h1>
            잘못된 게시글 주소입니다.
          </h1>
          <Link to="/">
            홈으로 돌아가기
          </Link>
        </div>
      </section>
    )
  }

  if (
    postQuery.isPending
    || session.isPending
  ) {
    return (
      <div className="route-loading">
        <LoaderCircle
          size={22}
          className="spin"
        />
        게시글 정보를 불러오는 중입니다.
      </div>
    )
  }

  if (postQuery.isError) {
    return (
      <section className="post-detail-page">
        <div className="container post-detail-state-card">
          <ShieldCheck size={34} />
          <h1>
            게시글을 불러오지 못했습니다.
          </h1>
          <p>
            {getPostErrorMessage(
              postQuery.error,
            )}
          </p>
          <Link to="/">
            홈으로 돌아가기
          </Link>
        </div>
      </section>
    )
  }

  const post = postQuery.data
  const user = session.data

  if (
    !user
    || user.userId !== post.authorId
  ) {
    return (
      <section className="post-detail-page">
        <div className="container post-detail-state-card">
          <ShieldCheck size={34} />
          <h1>
            수정 권한이 없습니다.
          </h1>
          <p>
            게시글 작성자만 내용을 수정할
            수 있습니다.
          </p>
          <Link
            to={`/posts/${post.postId}`}
          >
            게시글로 돌아가기
          </Link>
        </div>
      </section>
    )
  }

  return (
    <PostEditForm
      key={post.updatedAt}
      post={post}
    />
  )
}