import { useEffect, useRef, useState, type ChangeEvent, type DragEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { Alert } from '../components/Alert'
import { AppShell } from '../components/AppShell'
import { ArrowIcon, CloseIcon, FileIcon, ShieldIcon, UploadIcon } from '../components/Icons'
import { ApiError, api } from '../lib/api'
import { navigate } from '../lib/route'

const MAX_SIZE = 10 * 1024 * 1024
const ALLOWED = ['image/jpeg', 'image/png']

export function DashboardPage() {
  const { user, token } = useAuth()
  const [file, setFile] = useState<File | null>(null)
  const [preview, setPreview] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [dragging, setDragging] = useState(false)
  const [showUploadGuide, setShowUploadGuide] = useState(false)
  const [pendingFile, setPendingFile] = useState<File | null>(null)
  const input = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (!file) { setPreview(''); return }
    const url = URL.createObjectURL(file)
    setPreview(url)
    return () => URL.revokeObjectURL(url)
  }, [file])

  useEffect(() => {
    if (!showUploadGuide) return
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') closeUploadGuide()
    }
    window.addEventListener('keydown', closeOnEscape)
    return () => window.removeEventListener('keydown', closeOnEscape)
  }, [showUploadGuide])

  function choose(candidate?: File) {
    setError('')
    if (!candidate) return
    if (!ALLOWED.includes(candidate.type)) return setError('Choose a JPEG or PNG image.')
    if (candidate.size > MAX_SIZE) return setError('The selected image is larger than 10 MB.')
    setFile(candidate)
  }

  function drop(event: DragEvent) {
    event.preventDefault()
    setDragging(false)
    const droppedFile = event.dataTransfer.files[0]
    if (!droppedFile) return
    setPendingFile(droppedFile)
    setShowUploadGuide(true)
  }

  function requestFileSelection() {
    setPendingFile(null)
    setShowUploadGuide(true)
  }

  function closeUploadGuide() {
    setShowUploadGuide(false)
    setPendingFile(null)
  }

  function continueToSelection() {
    setShowUploadGuide(false)
    if (pendingFile) {
      choose(pendingFile)
      setPendingFile(null)
      return
    }
    if (input.current) {
      input.current.value = ''
      input.current.click()
    }
  }

  async function submit() {
    if (!file || !token) return
    setError('')
    setBusy(true)
    try {
      const prediction = await api.createPrediction(file, token)
      navigate(`/results/${prediction.id}`)
    } catch (exception) {
      setError(exception instanceof ApiError ? exception.message : 'The image could not be processed.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <AppShell active="dashboard">
      <section className="page-heading dashboard-heading">
        <div><span className="eyebrow">New analysis</span><h1>Good {greeting()}, {user?.fullName.split(' ')[0]}.</h1><p>Choose one musculoskeletal X-ray for an AI-assisted second-opinion review.</p></div>
        <div className="privacy-chip"><ShieldIcon /><span><strong>Protected upload</strong>Visible only to your account</span></div>
      </section>
      <section className="workflow-card">
        <div className="stepper" aria-label="Analysis progress"><span className="current"><b>1</b>Choose image</span><i></i><span><b>2</b>Review</span><i></i><span><b>3</b>Result</span></div>
        {error && <Alert>{error}</Alert>}
        {!file ? (
          <div className={dragging ? 'dropzone dragging' : 'dropzone'} onDragOver={event => { event.preventDefault(); setDragging(true) }} onDragLeave={() => setDragging(false)} onDrop={drop}>
            <span className="upload-orbit"><UploadIcon /></span>
            <h2>Upload an X-ray image</h2>
            <p>Drag and drop the image here, or choose it from your device.</p>
            <button className="secondary-button" onClick={requestFileSelection}>Choose image</button>
            <input ref={input} className="visually-hidden" type="file" accept="image/jpeg,image/png" onChange={(event: ChangeEvent<HTMLInputElement>) => choose(event.target.files?.[0])} />
            <small>JPEG or PNG · Maximum 10 MB · Minimum 128 × 128 px</small>
          </div>
        ) : (
          <div className="review-grid">
            <div className="image-preview"><img src={preview} alt="Preview of the selected X-ray" /><span>Image preview</span></div>
            <div className="review-details">
              <span className="eyebrow">Ready to review</span><h2>Check your image</h2>
              <div className="file-summary"><FileIcon /><span><strong>{file.name}</strong>{(file.size / 1024 / 1024).toFixed(2)} MB · {file.type.replace('image/', '').toUpperCase()}</span><button aria-label="Remove selected image" onClick={() => setFile(null)}><CloseIcon /></button></div>
              <div className="consent-box"><ShieldIcon /><p>By continuing, you confirm this is an X-ray image you are permitted to upload. The software-generated result must be reviewed by a qualified professional.</p></div>
              <button className="primary-button" disabled={busy} onClick={submit}>{busy ? 'Validating and processing…' : 'Analyse image'}<ArrowIcon /></button>
              <button className="text-button centered" disabled={busy} onClick={requestFileSelection}>Choose a different image</button>
              <input ref={input} className="visually-hidden" type="file" accept="image/jpeg,image/png" onChange={(event: ChangeEvent<HTMLInputElement>) => choose(event.target.files?.[0])} />
            </div>
          </div>
        )}
      </section>
      <section className="how-it-works"><h2>What happens next</h2><div><article><span>01</span><h3>Image validation</h3><p>The server checks the file type, size and whether the image can be decoded safely.</p></article><article><span>02</span><h3>Model review</h3><p>The backend passes the protected image to the versioned inference service.</p></article><article><span>03</span><h3>Bounded result</h3><p>You receive the class, confidence and clearly limited system-defined category.</p></article></div></section>
      {showUploadGuide && (
        <div className="modal-backdrop" role="presentation" onMouseDown={closeUploadGuide}>
          <section className="upload-guide" role="dialog" aria-modal="true" aria-labelledby="upload-guide-title" onMouseDown={event => event.stopPropagation()}>
            <button className="modal-close" type="button" aria-label="Close image guidance" onClick={closeUploadGuide}><CloseIcon /></button>
            <span className="upload-guide__icon"><UploadIcon /></span>
            <span className="eyebrow">Before you upload</span>
            <h2 id="upload-guide-title">Use a clear X-ray image.</h2>
            <p>A clean, complete image helps the system review it more accurately.</p>
            <ul className="upload-guide__checklist">
              <li><span>1</span><div><strong>Use the original digital X-ray when possible.</strong><p>Avoid screenshots, images copied from messaging apps and heavily compressed files.</p></div></li>
              <li><span>2</span><div><strong>Make sure it is sharp and fully visible.</strong><p>Keep the whole X-ray in frame without blur, glare, reflections or objects covering it.</p></div></li>
              <li><span>3</span><div><strong>Scan through a document-scanning app.</strong><p>If you only have a photo or printed film, a document-scanning app such as <b>CamScanner</b> can help crop and straighten it.</p></div></li>
            </ul>
            <div className="upload-guide__notice"><ShieldIcon /><p>Remove or cover any visible personal details before uploading if they are not needed for your review.</p></div>
            <div className="upload-guide__actions">
              <button className="secondary-button" type="button" onClick={closeUploadGuide}>Cancel</button>
              <button className="primary-button" type="button" autoFocus onClick={continueToSelection}>{pendingFile ? 'Use this image' : 'Continue to image selection'}<ArrowIcon /></button>
            </div>
          </section>
        </div>
      )}
    </AppShell>
  )
}

function greeting() {
  const hour = new Date().getHours()
  return hour < 12 ? 'morning' : hour < 18 ? 'afternoon' : 'evening'
}
