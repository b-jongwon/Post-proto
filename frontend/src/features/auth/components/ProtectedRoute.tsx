import { LoaderCircle } from 'lucide-react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useSession } from '@/features/auth/hooks/useAuth'

export function ProtectedRoute() {
  const location = useLocation()
  const session = useSession()

  if (session.isPending) {
    return (
      <div className="route-loading" role="status" aria-live="polite">
        <LoaderCircle className="spin" size={26} />
        <span>로그인 상태를 확인하고 있어요.</span>
      </div>
    )
  }

  if (!session.data) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}
