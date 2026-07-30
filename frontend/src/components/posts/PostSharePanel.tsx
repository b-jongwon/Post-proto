import {
  CheckCircle2,
  Copy,
  Globe2,
  QrCode,
  Share2,
  X,
} from 'lucide-react'
import QRCode from 'qrcode'
import {
  useEffect,
  useMemo,
  useState,
} from 'react'

export function PostSharePanel({
  title,
}: {
  title: string
}) {
  const [copied, setCopied] = useState(false)
  const [showQr, setShowQr] = useState(false)
  const [qrDataUrl, setQrDataUrl] =
    useState<string | null>(null)

  const shareUrl =
    typeof window === 'undefined'
      ? ''
      : window.location.href

  const encodedUrl = useMemo(
    () => encodeURIComponent(shareUrl),
    [shareUrl],
  )
  const encodedTitle = useMemo(
    () => encodeURIComponent(title),
    [title],
  )

  useEffect(() => {
    if (!showQr || !shareUrl) {
      return
    }

    let active = true

    void QRCode.toDataURL(shareUrl, {
      width: 260,
      margin: 2,
      color: {
        dark: '#172033',
        light: '#ffffff',
      },
      errorCorrectionLevel: 'M',
    }).then((dataUrl) => {
      if (active) {
        setQrDataUrl(dataUrl)
      }
    })

    return () => {
      active = false
    }
  }, [shareUrl, showQr])

  const handleCopy = async () => {
    await navigator.clipboard.writeText(shareUrl)
    setCopied(true)
    window.setTimeout(() => setCopied(false), 1800)
  }

  const handleNativeShare = async () => {
    if (navigator.share) {
      await navigator.share({
        title,
        text: `${title} · FactHub`,
        url: shareUrl,
      })
      return
    }

    await handleCopy()
  }

  return (
    <section
      id="post-share"
      className="post-share-panel"
      aria-label="게시글 공유"
    >
      <div className="post-share-panel__copy">
        <span>SHARE</span>
        <strong>이 팩트체크를 함께 확인해보세요</strong>
      </div>

      <div className="post-share-panel__actions">
        <button type="button" onClick={handleCopy}>
          {copied ? (
            <CheckCircle2 size={17} />
          ) : (
            <Copy size={17} />
          )}
          {copied ? '복사 완료' : '링크 복사'}
        </button>

        <button
          type="button"
          onClick={() => void handleNativeShare()}
        >
          <Share2 size={17} />
          SNS 공유
        </button>

        <a
          href={`https://twitter.com/intent/tweet?text=${encodedTitle}&url=${encodedUrl}`}
          target="_blank"
          rel="noreferrer"
          aria-label="X에 공유"
        >
          <X size={17} />
          X
        </a>

        <a
          href={`https://www.facebook.com/sharer/sharer.php?u=${encodedUrl}`}
          target="_blank"
          rel="noreferrer"
          aria-label="Facebook에 공유"
        >
          <Globe2 size={17} />
          Facebook
        </a>

        <button
          type="button"
          onClick={() => setShowQr((value) => !value)}
          aria-expanded={showQr}
        >
          <QrCode size={17} />
          QR
        </button>
      </div>

      {showQr && (
        <div className="post-share-panel__qr">
          {qrDataUrl ? (
            <img
              src={qrDataUrl}
              alt={`${title} 게시글 QR 코드`}
              width={180}
              height={180}
            />
          ) : (
            <span>QR 코드를 만드는 중...</span>
          )}
          <p>
            휴대폰 카메라로 스캔하면 이 게시글로
            이동합니다.
          </p>
        </div>
      )}
    </section>
  )
}
