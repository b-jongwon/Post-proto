import { ArrowLeft, Check, LockKeyhole, Mail, UserRound } from 'lucide-react'
import { Link } from 'react-router-dom'

export function SignupPage() {
  return (
    <section className="simple-auth-page">
      <div className="simple-auth-page__card">
        <Link to="/" className="back-link"><ArrowLeft size={17} /> 홈으로</Link>
        <div className="auth-form-card__heading">
          <span className="auth-step">01 · 계정 만들기</span>
          <h1>FactHub를 시작해보세요</h1>
          <p>1분이면 가입하고 팩트체크 커뮤니티에 참여할 수 있어요.</p>
        </div>
        <form className="auth-form" onSubmit={(event) => event.preventDefault()}>
          <label>
            <span>닉네임</span>
            <div className="input-shell">
              <UserRound size={18} />
              <input type="text" placeholder="2~20자 닉네임" autoComplete="nickname" />
            </div>
          </label>
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
              <input type="password" placeholder="안전한 비밀번호를 입력하세요" autoComplete="new-password" />
            </div>
          </label>
          <div className="password-guide">
            <span><Check size={14} /> 8자 이상</span>
            <span><Check size={14} /> 영문·숫자 조합</span>
          </div>
          <button type="submit" className="button button--primary button--lg button--full">
            가입하고 시작하기
          </button>
        </form>
        <p className="auth-form-card__footer">
          이미 계정이 있나요? <Link to="/login">로그인</Link>
        </p>
        <div className="stage-notice">2단계에서 회원가입 API와 유효성 검증이 연결됩니다.</div>
      </div>
    </section>
  )
}
