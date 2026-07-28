import { ShieldCheck } from 'lucide-react'
import { Link } from 'react-router-dom'

type FactHubLogoProps = {
  compact?: boolean
}

export function FactHubLogo({ compact = false }: FactHubLogoProps) {
  return (
    <Link to="/" className="brand-logo" aria-label="FactHub 홈으로 이동">
      <span className="brand-logo__mark" aria-hidden="true">
        <ShieldCheck size={20} strokeWidth={2.5} />
      </span>
      {!compact && (
        <span className="brand-logo__wordmark">
          Fact<span>Hub</span>
        </span>
      )}
    </Link>
  )
}
