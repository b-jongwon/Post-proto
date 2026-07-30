import { createBrowserRouter } from 'react-router-dom'
import App from '@/App'
import { ProtectedRoute } from '@/features/auth/components/ProtectedRoute'
import { RootLayout } from '@/layouts/RootLayout'
import {
    AdminPage,
    HomePage,
    LoginPage,
    MyPage,
    NotFoundPage,
    NotificationsPage,
    PageSuspense,
    PostCreatePage,
    PostDetailPage,
    PostEditPage,
    SignupPage,
} from '@/router/LazyPages'

function page(element: React.ReactNode) {
    return <PageSuspense>{element}</PageSuspense>
}

export const router = createBrowserRouter([
    {
        element: <App />,
        children: [
            {
                element: <RootLayout />,
                children: [
                    {
                        path: '/',
                        element: page(<HomePage />),
                    },
                    {
                        path: '/posts/:postId',
                        element: page(<PostDetailPage />),
                    },
                    {
                        path: '/login',
                        element: page(<LoginPage />),
                    },
                    {
                        path: '/signup',
                        element: page(<SignupPage />),
                    },
                    {
                        element: <ProtectedRoute />,
                        children: [
                            {
                                path: '/posts/new',
                                element: page(<PostCreatePage />),
                            },
                            {
                                path: '/posts/:postId/edit',
                                element: page(<PostEditPage />),
                            },
                            {
                                path: '/me',
                                element: page(<MyPage />),
                            },
                            {
                                path: '/notifications',
                                element: page(<NotificationsPage />),
                            },
                            {
                                path: '/admin',
                                element: page(<AdminPage />),
                            },
                        ],
                    },
                    {
                        path: '*',
                        element: page(<NotFoundPage />),
                    },
                ],
            },
        ],
    },
])
