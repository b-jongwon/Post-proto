import { createBrowserRouter } from 'react-router-dom'
import App from '@/App'
import { RootLayout } from '@/layouts/RootLayout'
import { HomePage } from '@/pages/HomePage'
import { LoginPage } from '@/pages/LoginPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { SignupPage } from '@/pages/SignupPage'

export const router = createBrowserRouter([
  {
    element: <App />,
    children: [
      {
        element: <RootLayout />,
        children: [
          { path: '/', element: <HomePage /> },
          { path: '/login', element: <LoginPage /> },
          { path: '/signup', element: <SignupPage /> },
          { path: '*', element: <NotFoundPage /> },
        ],
      },
    ],
  },
])
