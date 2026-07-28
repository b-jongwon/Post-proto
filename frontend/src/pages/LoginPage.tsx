import { ArrowLeft, Eye, LockKeyhole, Mail, ShieldCheck } from 'lucide-react'
import { Link } from 'react-router-dom'
import { FactHubLogo } from '@/components/brand/FactHubLogo'

export function LoginPage() {
  return (
    <section className="auth-page">
      <div className="auth-page__aside">
        <div className="auth-page__aside-inner">
          <FactHubLogo />
          <div className="auth-page__aside-copy">
            <BadgeLine />
            <h1>확신보다 먼저,<br />근거를 확인하는 습관</h1>
            <p>FactHub에서 신뢰할 수 있는 정보와 검증 과정을 한눈에 확인하세요.</p>
          </div>
          <div className="auth-page__trust-card">
            <ShieldCheck size={22} />
            <div>
              <strong>출처 중심의 투명한 분석</strong>
              <span>모든 판정은 근거와 함께 제공됩니다.</span>
            </div>
          </div>
        </div>
      </div>

      <div className="auth-page__content">
        <div className="auth-form-card">
          <Link to="/" className="back-link"><ArrowLeft size={17} /> 홈으로</Link>
          <div className="auth-form-card__heading">
            <h2>다시 만나서 반가워요</h2>
            <p>FactHub 계정으로 로그인하세요.</p>
          </div>
          <form className="auth-form" onSubmit={(event) => event.preventDefault()}>
            <label>
              <span>이메일</span>
              <div className="input-shell">
                <Mail size={18} />
                <input type="email" placeholder="name@example.com" autoComplete="email" />
              </div>
            </label>
            <label>
              <span>비밀번호</span>
              <div className="input-shell">
                <LockKeyhole size={18} />
                <input type="password" placeholder="비밀번호를 입력하세요" autoComplete="current-password" />
                <button type="button" aria-label="비밀번호 보기"><Eye size={18} /></button>
              </div>
            </label>
            <div className="auth-form__options">
              <label className="checkbox-label"><input type="checkbox" /> 로그인 유지</label>
              <button type="button" className="text-link">비밀번호 찾기</button>
            </div>
            <button type="submit" className="button button--primary button--lg button--full">
              로그인
            </button>
          </form>
          <p className="auth-form-card__footer">
            아직 계정이 없나요? <Link to="/signup">회원가입</Link>
          </p>
          <div className="stage-notice">2단계에서 실제 세션 로그인 API가 연결됩니다.</div>
        </div>
      </div>
    </section>
  )
}

function BadgeLine() {
  return <span className="auth-badge">FACTHUB VERIFIED COMMUNITY</span>
}
