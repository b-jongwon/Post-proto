import {
    lazy,
    Suspense,
    type ReactNode,
} from 'react'

export const HomePage = lazy(() =>
    import('@/pages/HomePage').then((module) => ({
        default: module.HomePage,
    })),
)

export const LoginPage = lazy(() =>
    import('@/pages/LoginPage').then((module) => ({
        default: module.LoginPage,
    })),
)

export const MyPage = lazy(() =>
    import('@/pages/MyPage').then((module) => ({
        default: module.MyPage,
    })),
)

export const NotFoundPage = lazy(() =>
    import('@/pages/NotFoundPage').then((module) => ({
        default: module.NotFoundPage,
    })),
)

export const PostCreatePage = lazy(() =>
    import('@/pages/PostCreatePage').then((module) => ({
        default: module.PostCreatePage,
    })),
)

export const PostDetailPage = lazy(() =>
    import('@/pages/PostDetailPage').then((module) => ({
        default: module.PostDetailPage,
    })),
)

export const PostEditPage = lazy(() =>
    import('@/pages/PostEditPage').then((module) => ({
        default: module.PostEditPage,
    })),
)

export const SignupPage = lazy(() =>
    import('@/pages/SignupPage').then((module) => ({
        default: module.SignupPage,
    })),
)

export const NotificationsPage = lazy(() =>
    import('@/pages/NotificationsPage').then((module) => ({
        default: module.NotificationsPage,
    })),
)

export const AdminPage = lazy(() =>
    import('@/pages/AdminPage').then((module) => ({
        default: module.AdminPage,
    })),
)

export function PageSuspense({
    children,
}: {
    children: ReactNode
}) {
    return (
        <Suspense
            fallback={(
                <div className="route-loading">
                    FactHub 화면을 준비하고 있습니다.
                </div>
            )}
        >
            {children}
        </Suspense>
    )
}

