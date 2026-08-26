import { useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { Alert } from '../components/Alert'
import { AuthShell } from '../components/AppShell'
import { ArrowIcon } from '../components/Icons'
import { ApiError, api } from '../lib/api'
import { navigate } from '../lib/route'
export function ProfessionalLoginPage() {
  const { accept } = useAuth(); const [username, setUsername] = useState(''); const [password, setPassword] = useState(''); const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  async function submit(event: FormEvent) { event.preventDefault(); setBusy(true); setError(''); try { accept(await api.professionalLogin({ username, password })); navigate('/professional/reviews') } catch (e) { setError(e instanceof ApiError ? e.message : 'Sign in could not be completed.') } finally { setBusy(false) } }
  return <AuthShell heading="Professional sign in" intro="Review consented fracture analyses shared by FractureCare users.">{error && <Alert>{error}</Alert>}<form className="form-stack" onSubmit={submit}><label>Username<input required value={username} onChange={e => setUsername(e.target.value)} autoComplete="username" /></label><label>Password<input required type="password" value={password} onChange={e => setPassword(e.target.value)} autoComplete="current-password" /></label><button className="primary-button" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}<ArrowIcon /></button></form><p className="auth-switch">Need an account? <button className="inline-link" onClick={() => navigate('/professional/register')}>Register as a professional</button></p><p className="auth-switch">Patient/user? <button className="inline-link" onClick={() => navigate('/login')}>Sign in here</button></p></AuthShell>
}
