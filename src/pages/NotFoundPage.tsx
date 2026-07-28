import { ArrowLeft, SearchX } from 'lucide-react'
import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <section className="not-found-page container">
      <div className="not-found-page__icon"><SearchX size={36} /></div>
      <span>404</span>
      <h1>요청한 페이지를 찾을 수 없어요</h1>
      <p>주소가 바뀌었거나 삭제된 페이지일 수 있습니다.</p>
      <Link to="/" className="button button--primary button--lg">
        <ArrowLeft size={17} /> 홈으로 돌아가기
      </Link>
    </section>
  )
}
