import { useQuery } from '@tanstack/react-query'
import {
  Clock3,
  Eye,
  Flame,
  Heart,
  MessageCircle,
  ThumbsUp,
  TrendingUp,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { getPostHighlights } from '@/features/posts/postApi'
import type {
  PostHighlights,
  PostSummary,
} from '@/features/posts/postTypes'

interface HighlightDefinition {
  key: keyof PostHighlights
  title: string
  description: string
  icon: typeof Flame
  tone: string
}

const definitions: HighlightDefinition[] = [
  {
    key: 'popular',
    title: '인기 게시글',
    description: '조회·좋아요·댓글을 함께 반영했어요',
    icon: Flame,
    tone: 'coral',
  },
  {
    key: 'mostLiked',
    title: '좋아요 TOP',
    description: '커뮤니티가 가장 많이 공감했어요',
    icon: ThumbsUp,
    tone: 'rose',
  },
  {
    key: 'mostViewed',
    title: '조회수 TOP',
    description: '지금 가장 많이 확인하고 있어요',
    icon: TrendingUp,
    tone: 'violet',
  },
  {
    key: 'latest',
    title: '최신 게시글',
    description: '방금 등록된 새로운 검증 요청이에요',
    icon: Clock3,
    tone: 'blue',
  },
]

function HighlightPost({
  post,
  rank,
}: {
  post: PostSummary
  rank: number
}) {
  return (
    <Link
      to={`/posts/${post.postId}`}
      className="highlight-post"
    >
      <span className="highlight-post__rank">
        {String(rank).padStart(2, '0')}
      </span>
      <div className="highlight-post__copy">
        <span>{post.category}</span>
        <strong>{post.title}</strong>
        <div>
          <span>
            <Eye size={13} />
            {post.viewCount.toLocaleString('ko-KR')}
          </span>
          <span>
            <Heart size={13} />
            {post.likeCount.toLocaleString('ko-KR')}
          </span>
          <span>
            <MessageCircle size={13} />
            {post.commentCount.toLocaleString('ko-KR')}
          </span>
        </div>
      </div>
    </Link>
  )
}

export function HomeHighlights() {
  const highlightsQuery = useQuery({
    queryKey: ['posts', 'highlights'],
    queryFn: ({ signal }) =>
      getPostHighlights(4, signal),
    staleTime: 30_000,
    retry: false,
  })

  return (
    <section
      className="home-highlights"
      aria-labelledby="home-highlights-title"
    >
      <div className="container">
        <header className="home-highlights__heading">
          <div>
            <span>DISCOVER FACTHUB</span>
            <h2 id="home-highlights-title">
              지금 FactHub에서 주목받는 이야기
            </h2>
          </div>
          <p>
            반응과 최신성을 기준으로 게시글을
            빠르게 탐색해보세요.
          </p>
        </header>

        <div className="highlight-board">
          {definitions.map((definition) => {
            const Icon = definition.icon
            const posts =
              highlightsQuery.data?.[definition.key]
              ?? []

            return (
              <article
                key={definition.key}
                className={`highlight-panel highlight-panel--${definition.tone}`}
              >
                <header>
                  <span className="highlight-panel__icon">
                    <Icon size={19} />
                  </span>
                  <div>
                    <h3>{definition.title}</h3>
                    <p>{definition.description}</p>
                  </div>
                </header>

                <div className="highlight-panel__list">
                  {highlightsQuery.isPending
                    ? Array.from({ length: 4 }).map(
                      (_, index) => (
                        <span
                          key={index}
                          className="highlight-post-skeleton"
                        />
                      ),
                    )
                    : posts.map((post, index) => (
                      <HighlightPost
                        key={post.postId}
                        post={post}
                        rank={index + 1}
                      />
                    ))}

                  {!highlightsQuery.isPending
                    && posts.length === 0 && (
                    <p className="highlight-panel__empty">
                      첫 게시글을 기다리고 있어요.
                    </p>
                  )}
                </div>
              </article>
            )
          })}
        </div>
      </div>
    </section>
  )
}

