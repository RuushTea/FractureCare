import { useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { Alert } from '../components/Alert'
import { AppShell } from '../components/AppShell'
import { ArrowIcon } from '../components/Icons'
import { ApiError, api } from '../lib/api'
import { navigate } from '../lib/route'

export function AdminProfessionalCreatePage() {
  const { token } = useAuth()
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (!token) return
    setBusy(true)
    setError('')
    setSuccess('')
    try {
      const created = await api.createProfessional({ fullName, email, password }, token)
      setSuccess(created.fullName + ' can now sign in with ' + created.email + '.')
      setFullName('')
      setEmail('')
      setPassword('')
    } catch (exception) {
      setError(exception instanceof ApiError ? exception.message : 'The professional account could not be created.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <AppShell active="admin">
      <section className="page-heading"><div><span className="eyebrow">Administrator</span><h1>Create a medical professional</h1><p>Provision an account that can sign in with email and review consented cases.</p></div><button className="secondary-button" onClick={() => navigate('/dashboard')}>Back to dashboard</button></section>
      <section className="workflow-card admin-form-card">
        {error && <Alert>{error}</Alert>}
        {success && <div className="success-message" role="status">{success}</div>}
        <form className="form-stack" onSubmit={submit}>
          <label>Full name<input required value={fullName} onChange={event => setFullName(event.target.value)} /></label>
          <label>Email address<input required type="email" value={email} onChange={event => setEmail(event.target.value)} autoComplete="off" /></label>
          <label>Temporary password<input required minLength={10} type="password" value={password} onChange={event => setPassword(event.target.value)} autoComplete="new-password" /><small>At least 10 characters with upper and lowercase letters and a number.</small></label>
          <button className="primary-button" disabled={busy}>{busy ? 'Creating account…' : 'Create professional account'}<ArrowIcon /></button>
        </form>
      </section>
    </AppShell>
  )
}
