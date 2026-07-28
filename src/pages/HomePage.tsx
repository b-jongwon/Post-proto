import type { CSSProperties } from 'react'
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
  Search,
  ShieldCheck,
  Sparkles,
  TrendingUp,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { Badge } from '@/components/ui/Badge'

const featuredPosts = [
  {
    id: 1,
    category: '사회',
    title: '대한민국의 수도는 부산이라는 주장은 사실일까?',
    excerpt:
      '공식 기록과 정부 통계를 토대로 게시글의 두 가지 핵심 주장을 검증했습니다.',
    verdict: '거짓',
    verdictTone: 'danger' as const,
    score: 0,
    author: '검증하는사람',
    time: '8분 전',
    views: '1.2천',
    comments: 24,
    likes: 86,
  },
  {
    id: 2,
    category: '테크',
    title: 'AI가 만든 뉴스는 원문 출처가 없으면 믿기 어렵다?',
    excerpt:
      '생성형 AI 시대에 출처 투명성이 왜 중요한지, 실제 사례와 함께 살펴봅니다.',
    verdict: '대체로 사실',
    verdictTone: 'success' as const,
    score: 88,
    author: '팩트러너',
    time: '31분 전',
    views: '842',
    comments: 13,
    likes: 54,
  },
  {
    id: 3,
    category: '생활',
    title: '커피를 마시면 물을 더 많이 마셔야 한다는 말',
    excerpt:
      '카페인의 이뇨 작용과 일상적인 수분 섭취의 관계를 연구 자료로 확인했습니다.',
    verdict: '맥락 필요',
    verdictTone: 'warning' as const,
    score: 62,
    author: '데이터산책',
    time: '1시간 전',
    views: '659',
    comments: 9,
    likes: 42,
  },
]

const trendingTopics = ['#AI뉴스', '#생활상식', '#경제지표', '#건강정보', '#자동차']

function ScoreRing({ score }: { score: number }) {
  return (
    <div className="score-ring" style={{ '--score': `${score * 3.6}deg` } as CSSProperties}>
      <div className="score-ring__inner">
        <strong>{score}</strong>
        <span>신뢰도</span>
      </div>
    </div>
  )
}

export function HomePage() {
  return (
    <>
      <section className="hero-section">
        <div className="hero-section__glow hero-section__glow--one" />
        <div className="hero-section__glow hero-section__glow--two" />
        <div className="container hero-section__inner">
          <div className="hero-copy">
            <Badge tone="brand">
              <Sparkles size={14} /> AI 기반 출처 검증
            </Badge>
            <h1>
              넘치는 정보 속에서,
              <br />
              <span>근거를 먼저 확인하세요.</span>
            </h1>
            <p>
              FactHub는 게시글의 핵심 주장을 분리하고, 신뢰할 수 있는 출처를 연결해
              판단에 필요한 맥락을 보여줍니다.
            </p>

            <div className="hero-search" role="search">
              <Search size={21} aria-hidden="true" />
              <input
                type="search"
                placeholder="확인하고 싶은 주장이나 이슈를 검색해보세요"
                aria-label="팩트체크 검색"
              />
              <button type="button">검색</button>
            </div>

            <div className="hero-trending" aria-label="인기 검색어">
              <span>지금 많이 찾는 주제</span>
              <div>
                {trendingTopics.map((topic) => (
                  <button key={topic} type="button">
                    {topic}
                  </button>
                ))}
              </div>
            </div>
          </div>

          <div className="hero-visual" aria-label="팩트체크 분석 예시">
            <div className="analysis-card analysis-card--main">
              <div className="analysis-card__top">
                <span className="analysis-card__eyebrow">
                  <ShieldCheck size={16} /> FactHub 분석
                </span>
                <Badge tone="danger">거짓</Badge>
              </div>
              <h2>대한민국의 수도는 부산이다</h2>
              <p>공식 기록과 정부 통계에서 확인된 내용과 일치하지 않습니다.</p>
              <div className="analysis-card__score">
                <ScoreRing score={0} />
                <div>
                  <strong>핵심 주장 2개 검증</strong>
                  <span>공공기관 출처 2개 연결</span>
                </div>
              </div>
              <div className="analysis-card__claim">
                <span><Check size={14} /> 주장 분리</span>
                <span><Check size={14} /> 근거 연결</span>
                <span><Check size={14} /> 출처 공개</span>
              </div>
            </div>
            <div className="analysis-float analysis-float--source">
              <span className="analysis-float__icon"><Bookmark size={17} /></span>
              <div>
                <strong>공식 출처 우선</strong>
                <span>국가기록원 · e-나라지표</span>
              </div>
            </div>
            <div className="analysis-float analysis-float--trend">
              <span className="analysis-float__icon"><TrendingUp size={17} /></span>
              <div>
                <strong>실시간 분석</strong>
                <span>근거와 맥락을 한눈에</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="content-section" id="discover">
        <div className="container content-grid">
          <div className="feed-column">
            <div className="section-heading">
              <div>
                <span className="section-heading__eyebrow">오늘의 이슈</span>
                <h2>지금 많이 보는 팩트체크</h2>
              </div>
              <button type="button" className="text-button">
                전체보기 <ArrowRight size={17} />
              </button>
            </div>

            <div className="feed-list">
              {featuredPosts.map((post) => (
                <article key={post.id} className="feed-card">
                  <div className="feed-card__body">
                    <div className="feed-card__meta">
                      <Badge>{post.category}</Badge>
                      <span><Clock3 size={14} /> {post.time}</span>
                    </div>
                    <h3>{post.title}</h3>
                    <p>{post.excerpt}</p>
                    <div className="feed-card__author">
                      <span className="avatar">{post.author.slice(0, 1)}</span>
                      <span>{post.author}</span>
                    </div>
                    <div className="feed-card__stats">
                      <span><Eye size={15} /> {post.views}</span>
                      <span><MessageCircle size={15} /> {post.comments}</span>
                      <span><Heart size={15} /> {post.likes}</span>
                    </div>
                  </div>
                  <div className="feed-card__verdict">
                    <Badge tone={post.verdictTone}>{post.verdict}</Badge>
                    <strong>{post.score}</strong>
                    <span>신뢰도 점수</span>
                    <button type="button" aria-label={`${post.title} 상세 보기`}>
                      <ChevronRight size={19} />
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </div>

          <aside className="side-column">
            <section className="side-card principle-card" id="service">
              <div className="side-card__icon"><ShieldCheck size={21} /></div>
              <h3>FactHub의 검증 원칙</h3>
              <p>판정보다 근거를 먼저 보여드립니다.</p>
              <ul>
                <li><span>01</span>주장을 작게 나눠 검증해요</li>
                <li><span>02</span>공식·1차 출처를 우선해요</li>
                <li><span>03</span>과거 분석 이력을 남겨요</li>
              </ul>
            </section>

            <section className="side-card daily-card">
              <div className="daily-card__header">
                <div>
                  <span>오늘의 분석</span>
                  <strong>128</strong>
                </div>
                <span className="daily-card__chart"><BarChart3 size={24} /></span>
              </div>
              <div className="daily-card__bar">
                <span style={{ width: '78%' }} />
              </div>
              <p>이번 주 목표의 78%를 검증했어요.</p>
            </section>

            <section className="side-card cta-card">
              <span className="cta-card__spark"><Sparkles size={22} /></span>
              <h3>확인하고 싶은 정보가 있나요?</h3>
              <p>글을 작성하면 AI가 핵심 주장과 근거를 함께 분석합니다.</p>
              <Link to="/signup">무료로 시작하기 <ArrowRight size={16} /></Link>
            </section>
          </aside>
        </div>
      </section>
    </>
  )
}
