import {
    ArrowLeft,
    CheckCircle2,
    FileText,
    Info,
    LoaderCircle,
    Send,
    ShieldCheck,
    Sparkles,
    Tag,
    Type,
} from 'lucide-react'
import {
    type FormEvent,
    useEffect,
    useMemo,
    useState,
} from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '@/api/ApiError'
import { createPost } from '@/features/posts/postApi'
import type { PostCreateRequest } from '@/features/posts/postTypes'
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

const initialForm: PostCreateRequest = {
    title: '',
    content: '',
    category: '',
}

type FieldErrors = Partial<
    Record<keyof PostCreateRequest, string>
>

function validateForm(
    form: PostCreateRequest,
): FieldErrors {
    const errors: FieldErrors = {}

    if (!form.title.trim()) {
        errors.title = '제목을 입력해 주세요.'
    } else if (form.title.length > TITLE_MAX_LENGTH) {
        errors.title = `제목은 ${TITLE_MAX_LENGTH}자 이하여야 합니다.`
    }

    if (!form.category.trim()) {
        errors.category = '카테고리를 선택해 주세요.'
    } else if (form.category.length > CATEGORY_MAX_LENGTH) {
        errors.category = `카테고리는 ${CATEGORY_MAX_LENGTH}자 이하여야 합니다.`
    }

    if (!form.content.trim()) {
        errors.content = '검증할 내용을 입력해 주세요.'
    } else if (form.content.length > CONTENT_MAX_LENGTH) {
        errors.content = `본문은 ${CONTENT_MAX_LENGTH.toLocaleString()}자 이하여야 합니다.`
    }

    return errors
}

export function PostCreatePage() {
    const navigate = useNavigate()

    const [form, setForm] = useState<PostCreateRequest>(
        initialForm,
    )
    const [fieldErrors, setFieldErrors] =
        useState<FieldErrors>({})
    const [submitError, setSubmitError] = useState('')
    const [isSubmitting, setIsSubmitting] =
        useState(false)

    const isDirty = useMemo(
        () =>
            form.title.length > 0
            || form.content.length > 0
            || form.category.length > 0,
        [form],
    )

    const completedFields = [
        Boolean(form.title.trim()),
        Boolean(form.category.trim()),
        Boolean(form.content.trim()),
    ].filter(Boolean).length

    const completionPercent = Math.round(
        (completedFields / 3) * 100,
    )

    const canSubmit =
        Boolean(form.title.trim())
        && Boolean(form.category.trim())
        && Boolean(form.content.trim())
        && form.title.length <= TITLE_MAX_LENGTH
        && form.category.length <= CATEGORY_MAX_LENGTH
        && form.content.length <= CONTENT_MAX_LENGTH
        && !isSubmitting

    useEffect(() => {
        const handleBeforeUnload = (
            event: BeforeUnloadEvent,
        ) => {
            if (!isDirty || isSubmitting) {
                return
            }

            event.preventDefault()
        }

        window.addEventListener(
            'beforeunload',
            handleBeforeUnload,
        )

        return () => {
            window.removeEventListener(
                'beforeunload',
                handleBeforeUnload,
            )
        }
    }, [isDirty, isSubmitting])

    const updateField = (
        field: keyof PostCreateRequest,
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
                '작성 중인 내용이 사라집니다. 페이지를 나가시겠어요?',
            )
        ) {
            return
        }

        navigate(-1)
    }

    const handleSubmit = async (
        event: FormEvent<HTMLFormElement>,
    ) => {
        event.preventDefault()

        const validationErrors = validateForm(form)

        if (Object.keys(validationErrors).length > 0) {
            setFieldErrors(validationErrors)
            return
        }

        setIsSubmitting(true)
        setSubmitError('')

        try {
            const createdPost = await createPost({
                title: form.title.trim(),
                category: form.category.trim(),
                content: form.content.trim(),
            })

            navigate(`/posts/${createdPost.postId}`, {
                replace: true,
            })
        } catch (error) {
            if (error instanceof ApiError) {
                setSubmitError(error.message)

                const backendFields: FieldErrors = {}

                if (error.fields.title) {
                    backendFields.title = error.fields.title
                }

                if (error.fields.category) {
                    backendFields.category =
                        error.fields.category
                }

                if (error.fields.content) {
                    backendFields.content =
                        error.fields.content
                }

                if (Object.keys(backendFields).length > 0) {
                    setFieldErrors(backendFields)
                }
            } else {
                setSubmitError(
                    '게시글을 등록하지 못했습니다. 잠시 후 다시 시도해 주세요.',
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
                        돌아가기
                    </button>

                    <div className="post-editor-heading">
            <span className="post-editor-heading__eyebrow">
              <Sparkles size={15} />
              새로운 팩트체크
            </span>

                        <h1>검증하고 싶은 정보를 작성하세요</h1>

                        <p>
                            핵심 주장이 명확할수록 AI가 더 정확하게
                            주장과 근거를 분리할 수 있습니다.
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
                                <span>게시글 작성</span>
                                <h2>검증할 내용 입력</h2>
                            </div>

                            <div className="post-editor-progress">
                                <div>
                                    <strong>{completionPercent}%</strong>
                                    <span>작성 완료</span>
                                </div>

                                <div className="post-editor-progress__track">
                  <span
                      style={{
                          width: `${completionPercent}%`,
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

                                <span
                                    className={
                                        form.title.length
                                        > TITLE_MAX_LENGTH
                                            ? 'is-over'
                                            : ''
                                    }
                                >
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
                                placeholder="예: 전기차 배터리는 겨울에 성능이 크게 떨어진다?"
                                maxLength={TITLE_MAX_LENGTH + 1}
                                aria-invalid={Boolean(
                                    fieldErrors.title,
                                )}
                                aria-describedby={
                                    fieldErrors.title
                                        ? 'post-title-error'
                                        : undefined
                                }
                            />

                            {fieldErrors.title && (
                                <p
                                    id="post-title-error"
                                    className="post-editor-field__error"
                                >
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
                                    aria-describedby={
                                        fieldErrors.category
                                            ? 'post-category-error'
                                            : undefined
                                    }
                                >
                                    <option value="">
                                        카테고리를 선택하세요
                                    </option>

                                    {categories.map((category) => (
                                        <option
                                            key={category}
                                            value={category}
                                        >
                                            {category}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {fieldErrors.category && (
                                <p
                                    id="post-category-error"
                                    className="post-editor-field__error"
                                >
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

                                <span
                                    className={
                                        form.content.length
                                        > CONTENT_MAX_LENGTH
                                            ? 'is-over'
                                            : ''
                                    }
                                >
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
                                placeholder={`검증하고 싶은 주장과 그 배경을 구체적으로 작성해 주세요.

예시)
최근 온라인에서 '전기차는 영하 10도에서 주행거리가 절반 이하로 줄어든다'는 글을 보았습니다. 모든 전기차에서 실제로 이 정도의 감소가 나타나는지 확인하고 싶습니다.`}
                                maxLength={CONTENT_MAX_LENGTH + 1}
                                aria-invalid={Boolean(
                                    fieldErrors.content,
                                )}
                                aria-describedby={
                                    fieldErrors.content
                                        ? 'post-content-error'
                                        : 'post-content-help'
                                }
                            />

                            <div className="post-editor-content-footer">
                                <p id="post-content-help">
                                    주장, 수치, 시기, 출처 정보를 함께
                                    적으면 분석 정확도가 높아집니다.
                                </p>

                                {fieldErrors.content && (
                                    <p
                                        id="post-content-error"
                                        className="post-editor-field__error"
                                    >
                                        {fieldErrors.content}
                                    </p>
                                )}
                            </div>
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
                                        등록 중
                                    </>
                                ) : (
                                    <>
                                        게시글 등록
                                        <Send size={17} />
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
                                    <span>미리보기</span>
                                    <h2>게시글 카드</h2>
                                </div>
                            </div>

                            <div className="post-editor-preview-card">
                <span className="post-editor-preview-card__category">
                  {form.category || '카테고리'}
                </span>

                                <h3>
                                    {form.title.trim()
                                        || '작성한 제목이 여기에 표시됩니다.'}
                                </h3>

                                <p>
                                    {form.content.trim()
                                        ? form.content
                                            .trim()
                                            .slice(0, 120)
                                        : '검증할 내용을 작성하면 게시글의 미리보기를 확인할 수 있습니다.'}

                                    {form.content.trim().length > 120
                                        ? '...'
                                        : ''}
                                </p>

                                <div className="post-editor-preview-card__footer">
                  <span className="post-editor-preview-avatar">
                    F
                  </span>
                                    <div>
                                        <strong>FactHub 사용자</strong>
                                        <span>방금 전</span>
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
                                    <span>작성 가이드</span>
                                    <h2>좋은 검증 요청</h2>
                                </div>
                            </div>

                            <ul>
                                <li>
                                    <CheckCircle2 size={17} />
                                    <div>
                                        <strong>하나의 핵심 주장</strong>
                                        <span>
                      무엇이 사실인지 명확하게 작성하세요.
                    </span>
                                    </div>
                                </li>

                                <li>
                                    <CheckCircle2 size={17} />
                                    <div>
                                        <strong>구체적인 수치와 시기</strong>
                                        <span>
                      날짜와 수치가 있으면 검증이 쉬워집니다.
                    </span>
                                    </div>
                                </li>

                                <li>
                                    <CheckCircle2 size={17} />
                                    <div>
                                        <strong>원문과 출처 정보</strong>
                                        <span>
                      어디서 본 내용인지 함께 적어주세요.
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