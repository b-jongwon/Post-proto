type SkeletonProps = {
  width?: string
  height?: string
  radius?: string
}

export function Skeleton({
  width = '100%',
  height = '16px',
  radius = '12px',
}: SkeletonProps) {
  return (
    <span
      className="skeleton"
      aria-hidden="true"
      style={{ width, height, borderRadius: radius }}
    />
  )
}
