import {
  Bell,
  Home,
  LogIn,
  Menu,
  PenLine,
  Search,
  UserRound,
} from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'
import { FactHubLogo } from '@/components/brand/FactHubLogo'

const desktopNavigation = [
  { to: '/', label: '홈' },
  { to: '/?category=issue', label: '이슈' },
  { to: '/?category=technology', label: '테크' },
  { to: '/?category=society', label: '사회' },
]

export function RootLayout() {
  return (
    <div className="app-shell">
      <header className="site-header">
        <div className="site-header__inner container">
          <FactHubLogo />

          <nav className="desktop-nav" aria-label="주요 메뉴">
            {desktopNavigation.map((item) => (
              <NavLink
                key={item.label}
                to={item.to}
                className={({ isActive }) =>
                  `desktop-nav__link${isActive && item.to === '/' ? ' is-active' : ''}`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className="site-header__actions">
            <button className="icon-button desktop-only" aria-label="알림">
              <Bell size={20} />
            </button>
            <NavLink to="/login" className="header-login-link">
              <LogIn size={18} />
              <span>로그인</span>
            </NavLink>
            <NavLink to="/signup" className="header-signup-link desktop-only">
              시작하기
            </NavLink>
            <button className="icon-button mobile-only" aria-label="메뉴 열기">
              <Menu size={22} />
            </button>
          </div>
        </div>
      </header>

      <main className="site-main">
        <Outlet />
      </main>

      <footer className="site-footer">
        <div className="container site-footer__inner">
          <FactHubLogo />
          <p>근거를 확인하고, 더 나은 판단을 만듭니다.</p>
          <div className="site-footer__links">
            <a href="#service">서비스 소개</a>
            <a href="#policy">운영 원칙</a>
            <a href="#privacy">개인정보 처리방침</a>
          </div>
          <small>© 2026 FactHub. All rights reserved.</small>
        </div>
      </footer>

      <nav className="mobile-tabbar" aria-label="모바일 메뉴">
        <NavLink to="/" aria-label="홈">
          <Home size={21} />
          <span>홈</span>
        </NavLink>
        <a href="#discover" aria-label="탐색">
          <Search size={21} />
          <span>탐색</span>
        </a>
        <a href="#write" className="mobile-tabbar__write" aria-label="글쓰기">
          <PenLine size={21} />
          <span>글쓰기</span>
        </a>
        <NavLink to="/login" aria-label="내 정보">
          <UserRound size={21} />
          <span>내 정보</span>
        </NavLink>
      </nav>
    </div>
  )
}
