import { createBrowserRouter } from 'react-router-dom'
import App from '@/App'
import { ProtectedRoute } from '@/features/auth/components/ProtectedRoute'
import { RootLayout } from '@/layouts/RootLayout'
import { HomePage } from '@/pages/HomePage'
import { LoginPage } from '@/pages/LoginPage'
import { MyPage } from '@/pages/MyPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { PostCreatePage } from '@/pages/PostCreatePage'
import { PostDetailPage } from '@/pages/PostDetailPage'
import { PostEditPage } from '@/pages/PostEditPage'
import { SignupPage } from '@/pages/SignupPage'

export const router = createBrowserRouter([
    {
        element: <App />,
        children: [
            {
                element: <RootLayout />,
                children: [
                    {
                        path: '/',
                        element: <HomePage />,
                    },
                    {
                        path: '/posts/:postId',
                        element: <PostDetailPage />,
                    },
                    {
                        path: '/login',
                        element: <LoginPage />,
                    },
                    {
                        path: '/signup',
                        element: <SignupPage />,
                    },
                    {
                        element: <ProtectedRoute />,
                        children: [
                            {
                                path: '/posts/new',
                                element: <PostCreatePage />,
                            },
                            {
                                path: '/posts/:postId/edit',
                                element: <PostEditPage />,
                            },
                            {
                                path: '/me',
                                element: <MyPage />,
                            },
                        ],
                    },
                    {
                        path: '*',
                        element: <NotFoundPage />,
                    },
                ],
            },
        ],
    },
])