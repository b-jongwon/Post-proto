import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router-dom'
import { queryClient } from '@/app/queryClient'
import { router } from '@/router/router'
import '@/styles/tokens.css'
import '@/styles/global.css'

const rootElement = document.getElementById('root')

if (!rootElement) {
  throw new Error('React root element를 찾을 수 없습니다.')
}

createRoot(rootElement).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </StrictMode>,
)
