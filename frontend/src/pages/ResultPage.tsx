import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { Alert } from '../components/Alert'
import { AppShell } from '../components/AppShell'
import { DownloadIcon, FileIcon, ShieldIcon } from '../components/Icons'
import { StatusPill } from '../components/StatusPill'
import { ApiError, api } from '../lib/api'
import { formatDate, label, percentage } from '../lib/format'
import { navigate } from '../lib/route'
import type { Prediction, ReportResponse } from '../types'

export function ResultPage({ id }: { id: number }) {
  const { token } = useAuth()
  const [prediction, setPrediction] = useState<Prediction | null>(null)
  const [error, setError] = useState('')
  const [downloading, setDownloading] = useState(false)
  const [askingGroq, setAskingGroq] = useState(false)
  const [explanationError, setExplanationError] = useState('')
  const [requestingReview, setRequestingReview] = useState(false)
  const [reviewError, setReviewError] = useState('')

  useEffect(() => {
    if (!token) return
    api.getPrediction(id, token).then(setPrediction).catch(exception => setError(exception instanceof ApiError ? exception.message : 'The result could not be loaded.'))
  }, [id, token])

  async function report() {
    if (!token) return
    setDownloading(true)
    setError('')
    try {
      const generated: ReportResponse = await api.createReport(id, token)
      await api.downloadReport(generated, token)
    } catch (exception) {
      setError(exception instanceof ApiError ? exception.message : 'The report could not be downloaded.')
    } finally {
      setDownloading(false)
    }
  }

  async function askGroq() {
    if (!token) return
    setAskingGroq(true)
    setExplanationError('')
    try {
      setPrediction(await api.explainPrediction(id, token))
    } catch (exception) {
      setExplanationError(exception instanceof ApiError ? exception.message : 'The explanation assistant could not be reached.')
    } finally {
      setAskingGroq(false)
    }
  }

  async function requestReview() {
    if (!token) return
    setRequestingReview(true); setReviewError('')
    try { const state = await api.requestProfessionalReview(id, token); setPrediction(current => current ? { ...current, professionalReview: state } : current) }
    catch (exception) { setReviewError(exception instanceof ApiError ? exception.message : 'The professional review request could not be completed.') }
    finally { setRequestingReview(false) }
  }

  return (
    <AppShell active="result">
      <section className="page-heading"><div><span className="eyebrow">Analysis result</span><h1>Your X-ray review</h1><p>{prediction ? `Completed ${formatDate(prediction.createdAt)}` : 'Loading the result…'}</p></div><button className="secondary-button" onClick={() => navigate('/dashboard')}>New analysis</button></section>
      {error && <Alert>{error}</Alert>}
      {!prediction ? <div className="loading-card"><span className="spinner"></span>Loading your result…</div> : prediction.status === 'FAILED' ? (
        <section className="result-card"><Alert>{prediction.failureMessage ?? 'The prediction service could not complete this request.'}</Alert><button className="primary-button" onClick={() => navigate('/dashboard')}>Try another image</button></section>
      ) : (
        <>
          <section className="result-hero">
            <div className="result-hero__main"><span className="eyebrow">System-defined category</span><div className="result-title"><StatusPill value={prediction.riskCategory} /><span className="confidence"><b>{percentage(prediction.confidence)}</b>model confidence</span></div><h2>{resultHeading(prediction)}</h2><p>{resultMessage(prediction)}</p></div>
            <div className="result-meta"><div><span>Prediction class</span><strong>{label(prediction.predictedClass)}</strong></div><div><span>Model version</span><strong>{prediction.modelVersion}</strong></div><div><span>Reference</span><strong>FC-{String(prediction.id).padStart(6, '0')}</strong></div></div>
          </section>
          <section className="result-grid result-grid--single">
            <aside className="report-card"><span className="report-icon"><FileIcon /></span><h2>Prediction report</h2><p>Download a PDF containing the result, confidence, model version and safety guidance.</p><button className="primary-button" onClick={report} disabled={downloading}>{downloading ? 'Preparing report…' : 'Download PDF'}<DownloadIcon /></button><small>Generated securely for your account</small></aside>
          </section>
          <section className="safety-banner"><ShieldIcon /><div><h2>What should I do next?</h2><p>Share the original X-ray and this result with a qualified medical professional. Seek urgent care for severe pain, deformity, numbness, heavy bleeding or other emergency symptoms.</p></div></section>
          {prediction.explanation ? <article className="ai-explanation-card groq-result">
            <div className="explanation-heading"><div><span className="eyebrow">Explanation assistant</span><h2>What this result means</h2></div><span className="explanation-source">{prediction.explanation.source === 'GROQ' ? 'Groq AI' : 'Safety fallback'}</span></div>
            <p className="explanation-summary">{prediction.explanation.summary}</p>
            <div className="explanation-details"><div><h3>About the confidence</h3><p>{prediction.explanation.confidenceMeaning}</p></div><div><h3>Suggested next step</h3><p>{prediction.explanation.nextStep}</p></div></div>
            {prediction.explanation.questionsForClinician.length > 0 && <div className="clinician-questions"><h3>Questions you could ask a clinician</h3><ul>{prediction.explanation.questionsForClinician.map(question => <li key={question}>{question}</li>)}</ul></div>}
            <small>This assistant explains the existing output. It does not inspect the X-ray or change the prediction.</small>
          </article> : <article className="groq-invitation">
            <span className="eyebrow">Optional explanation</span>
            <h2>Do you want to ask Groq AI about this result?</h2>
            <p>Groq can turn the prediction class, category and confidence into a simpler explanation. Your X-ray and account details will not be sent.</p>
            {explanationError && <Alert>{explanationError}</Alert>}
            <button className="primary-button" onClick={askGroq} disabled={askingGroq}>{askingGroq ? 'Asking Groq AI...' : 'Ask Groq AI about this result'}</button>
            <small>Groq explains the existing result only. It cannot inspect the image, change the prediction or provide a diagnosis.</small>
          </article>}
          {prediction.professionalReview?.status === 'COMPLETED' ? <article className="ai-explanation-card groq-result"><div className="explanation-heading"><div><span className="eyebrow">Medical professional review</span><h2>Independent review completed</h2></div><span className="explanation-source">Completed</span></div><p><b>Does the X-ray show a fracture?</b> {professionalFractureAnswer(prediction)}</p><p><b>Professional comments:</b> {prediction.professionalReview.comment}</p><small>Reviewed {prediction.professionalReview.completedAt ? formatDate(prediction.professionalReview.completedAt) : ''}{prediction.professionalReview.reviewerName ? ` by ${prediction.professionalReview.reviewerName}` : ''}</small></article> : prediction.professionalReview?.status === 'PENDING' ? <article className="groq-invitation"><span className="eyebrow">Medical professional review</span><h2>Awaiting medical professional review</h2><p>Your consented case is in the professional queue. The AI result and its safety guidance remain unchanged.</p></article> : <article className="groq-invitation"><span className="eyebrow">Optional second opinion</span><h2>Review from a medical professional</h2><p>The original X-ray, AI prediction, confidence and relevant result information will be shared with a registered medical professional. Your explicit consent is required.</p>{reviewError && <Alert>{reviewError}</Alert>}<button className="primary-button" onClick={requestReview} disabled={requestingReview}>{requestingReview ? 'Requesting review…' : 'Consent & request professional review'}</button><small>You can request this independently of the Groq explanation.</small></article>}
        </>
      )}
    </AppShell>
  )
}

function resultHeading(prediction: Prediction) {
  if (prediction.riskCategory === 'NO_FRACTURE') return 'No fracture pattern was identified by the system.'
  if (prediction.riskCategory === 'LOW_RISK') return 'A possible single fracture pattern was identified.'
  return 'Possible multiple fracture patterns were identified.'
}

function resultMessage(prediction: Prediction) {
  return prediction.riskCategory === 'NO_FRACTURE'
    ? 'This does not rule out a subtle or occult fracture. Professional interpretation remains important.'
    : 'This is not a confirmed diagnosis or recovery prediction. Please arrange professional assessment.'
}

function professionalFractureAnswer(prediction: Prediction) {
  const aiShowsFracture = prediction.predictedClass !== 'NO_FRACTURE'
  return prediction.professionalReview?.agreesWithAi === aiShowsFracture ? 'Yes' : 'No'
}

