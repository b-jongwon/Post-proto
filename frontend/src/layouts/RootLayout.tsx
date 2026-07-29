import {
    Bell,
    Home,
    LogIn,
    LogOut,
    Menu,
    PenLine,
    Search,
    UserRound,
} from 'lucide-react'
import '@/styles/header-actions.css'
import {
    NavLink,
    Outlet,
    useNavigate,
} from 'react-router-dom'
import { FactHubLogo } from '@/components/brand/FactHubLogo'
import {
    useLogout,
    useSession,
} from '@/features/auth/hooks/useAuth'

const desktopNavigation = [
    {
        to: '/',
        label: '홈',
    },
    {
        to: '/?category=issue',
        label: '이슈',
    },
    {
        to: '/?category=technology',
        label: '테크',
    },
    {
        to: '/?category=society',
        label: '사회',
    },
]

export function RootLayout() {
    const navigate = useNavigate()
    const session = useSession()
    const logoutMutation = useLogout()
    const user = session.data

    const handleLogout = async () => {
        await logoutMutation.mutateAsync()

        navigate('/', {
            replace: true,
        })
    }

    return (
        <div className="app-shell">
            <header className="site-header">
                <div className="site-header__inner container">
                    <FactHubLogo />

                    <nav
                        className="desktop-nav"
                        aria-label="주요 메뉴"
                    >
                        {desktopNavigation.map(
                            (item) => (
                                <NavLink
                                    key={item.label}
                                    to={item.to}
                                    className={({
                                                    isActive,
                                                }) =>
                                        `desktop-nav__link${
                                            isActive
                                            && item.to === '/'
                                                ? ' is-active'
                                                : ''
                                        }`
                                    }
                                >
                                    {item.label}
                                </NavLink>
                            ),
                        )}
                    </nav>

                    <div className="site-header__actions">
                        <button
                            type="button"
                            className="icon-button desktop-only"
                            aria-label="알림"
                        >
                            <Bell size={20} />
                        </button>

                        {session.isPending ? (
                            <span
                                className="header-session-loading"
                                aria-label="로그인 상태 확인 중"
                            />
                        ) : user ? (
                            <>
                                <NavLink
                                    to="/posts/new"
                                    className="header-write-link desktop-only"
                                >
                                    <PenLine size={16} />
                                    <span>글쓰기</span>
                                </NavLink>

                                <NavLink
                                    to="/me"
                                    className="header-account-link"
                                >
                  <span className="header-avatar">
                    {user.nickname
                        .slice(0, 1)
                        .toUpperCase()}
                  </span>

                                    <span className="header-account-copy desktop-only">
                    <strong>
                      {user.nickname}
                    </strong>

                    <small>
                      {user.role === 'ADMIN'
                          ? '관리자'
                          : '내 정보'}
                    </small>
                  </span>
                                </NavLink>

                                <button
                                    type="button"
                                    className="icon-button desktop-only"
                                    aria-label="로그아웃"
                                    onClick={handleLogout}
                                    disabled={
                                        logoutMutation.isPending
                                    }
                                >
                                    <LogOut size={19} />
                                </button>
                            </>
                        ) : (
                            <>
                                <NavLink
                                    to="/login"
                                    className="header-login-link"
                                >
                                    <LogIn size={18} />
                                    <span>로그인</span>
                                </NavLink>

                                <NavLink
                                    to="/login"
                                    className="header-write-link desktop-only"
                                >
                                    <PenLine size={16} />
                                    <span>글쓰기</span>
                                </NavLink>

                                <NavLink
                                    to="/signup"
                                    className="header-signup-link desktop-only"
                                >
                                    시작하기
                                </NavLink>
                            </>
                        )}

                        <button
                            type="button"
                            className="icon-button mobile-only"
                            aria-label="메뉴 열기"
                        >
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

                    <p>
                        근거를 확인하고, 더 나은
                        판단을 만듭니다.
                    </p>

                    <div className="site-footer__links">
                        <a href="/#service">
                            서비스 소개
                        </a>
                        <a href="/#policy">
                            운영 원칙
                        </a>
                        <a href="/#privacy">
                            개인정보 처리방침
                        </a>
                    </div>

                    <small>
                        © 2026 FactHub. All rights
                        reserved.
                    </small>
                </div>
            </footer>

            <nav
                className="mobile-tabbar"
                aria-label="모바일 메뉴"
            >
                <NavLink
                    to="/"
                    aria-label="홈"
                >
                    <Home size={21} />
                    <span>홈</span>
                </NavLink>

                <a
                    href="/#discover"
                    aria-label="탐색"
                >
                    <Search size={21} />
                    <span>탐색</span>
                </a>

                <NavLink
                    to={
                        user
                            ? '/posts/new'
                            : '/login'
                    }
                    className="mobile-tabbar__write"
                    aria-label="글쓰기"
                >
                    <PenLine size={21} />
                    <span>글쓰기</span>
                </NavLink>

                <NavLink
                    to={
                        user
                            ? '/me'
                            : '/login'
                    }
                    aria-label="내 정보"
                >
                    <UserRound size={21} />

                    <span>
            {user
                ? '내 정보'
                : '로그인'}
          </span>
                </NavLink>
            </nav>
        </div>
    )
}