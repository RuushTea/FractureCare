import { useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { Alert } from '../components/Alert'
import { AuthShell } from '../components/AppShell'
import { ArrowIcon } from '../components/Icons'
import { ApiError, api } from '../lib/api'
import { navigate } from '../lib/route'

export function LoginPage() {
  const { accept } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError('')
    setBusy(true)
    try {
      accept(await api.login({ email, password }))
      navigate('/dashboard')
    } catch (exception) {
      setError(exception instanceof ApiError ? exception.message : 'Sign in could not be completed.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <AuthShell heading="Sign in to your account" intro="Continue to a new X-ray review or return to an earlier report.">
      {error && <Alert>{error}</Alert>}
      <form className="form-stack" onSubmit={submit}>
        <label>Email address<input type="email" autoComplete="email" required value={email} onChange={event => setEmail(event.target.value)} placeholder="you@example.com" /></label>
        <label>Password<input type="password" autoComplete="current-password" required value={password} onChange={event => setPassword(event.target.value)} placeholder="Enter your password" /></label>
        <button className="primary-button" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}<ArrowIcon /></button>
      </form>
      <p className="auth-switch">New to FractureCare? <button className="inline-link" onClick={() => navigate('/register')}>Create an account</button></p>
      <p className="auth-switch"><button className="inline-link" onClick={() => navigate('/professional/login')}>Medical professional sign in</button></p>
    </AuthShell>
  )
}
