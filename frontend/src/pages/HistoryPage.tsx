import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { Alert } from '../components/Alert'
import { AppShell } from '../components/AppShell'
import { EyeIcon, HistoryIcon } from '../components/Icons'
import { StatusPill } from '../components/StatusPill'
import { ApiError, api } from '../lib/api'
import { formatDate, percentage } from '../lib/format'
import { navigate } from '../lib/route'
import type { PageResponse, Prediction } from '../types'

export function HistoryPage() {
  const { token } = useAuth()
  const [page, setPage] = useState(0)
  const [history, setHistory] = useState<PageResponse<Prediction> | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!token) return
    setError('')
    api.history(page, token).then(setHistory).catch(exception => setError(exception instanceof ApiError ? exception.message : 'History could not be loaded.'))
  }, [page, token])

  return (
    <AppShell active="history">
      <section className="page-heading"><div><span className="eyebrow">Prediction history</span><h1>Your previous reviews</h1><p>Only results associated with your account appear here.</p></div><button className="primary-button compact" onClick={() => navigate('/dashboard')}>New analysis</button></section>
      {error && <Alert>{error}</Alert>}
      <section className="history-card">
        {!history ? <div className="loading-card"><span className="spinner"></span>Loading history…</div>
          : history.content.length === 0 ? <div className="empty-state"><HistoryIcon /><h2>No analyses yet</h2><p>Your completed X-ray reviews will appear here.</p><button className="primary-button compact" onClick={() => navigate('/dashboard')}>Upload an X-ray</button></div>
            : <><div className="history-list">{history.content.map(prediction => <article key={prediction.id} className="history-row"><div className="history-ref">FC-{String(prediction.id).padStart(6, '0')}</div><div><strong>{prediction.originalFileName}</strong><span>{formatDate(prediction.createdAt)}</span></div><StatusPill value={prediction.status === 'COMPLETED' ? prediction.riskCategory : prediction.status} /><div className="history-confidence"><strong>{percentage(prediction.confidence)}</strong><span>confidence</span></div><button className="icon-button" onClick={() => navigate(`/results/${prediction.id}`)} aria-label={`View result ${prediction.id}`}><EyeIcon />View</button></article>)}</div>
              <div className="pagination"><span>Page {history.page + 1} of {Math.max(history.totalPages, 1)} · {history.totalElements} result{history.totalElements === 1 ? '' : 's'}</span><div><button className="secondary-button compact" disabled={history.first} onClick={() => setPage(current => current - 1)}>Previous</button><button className="secondary-button compact" disabled={history.last} onClick={() => setPage(current => current + 1)}>Next</button></div></div></>}
      </section>
    </AppShell>
  )
}
