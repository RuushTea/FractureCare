import { useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { Alert } from '../components/Alert'
import { AuthShell } from '../components/AppShell'
import { ArrowIcon } from '../components/Icons'
import { ApiError, api } from '../lib/api'
import { navigate } from '../lib/route'
export function ProfessionalRegisterPage() {
  const { accept } = useAuth(); const [fields, setFields] = useState({ fullName: '', email: '', username: '', password: '' }); const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  const update = (name: keyof typeof fields, value: string) => setFields(f => ({ ...f, [name]: value }))
  async function submit(event: FormEvent) { event.preventDefault(); setBusy(true); setError(''); try { accept(await api.professionalRegister(fields)); navigate('/professional/reviews') } catch (e) { setError(e instanceof ApiError ? e.message : 'Registration could not be completed.') } finally { setBusy(false) } }
  return <AuthShell heading="Professional registration" intro="Create a medical professional account to review consented cases.">{error && <Alert>{error}</Alert>}<form className="form-stack" onSubmit={submit}><label>Full name<input required value={fields.fullName} onChange={e => update('fullName', e.target.value)} /></label><label>Email address<input required type="email" value={fields.email} onChange={e => update('email', e.target.value)} /></label><label>Username<input required value={fields.username} onChange={e => update('username', e.target.value)} /></label><label>Password<input required minLength={10} type="password" value={fields.password} onChange={e => update('password', e.target.value)} /><small>At least 10 characters with upper and lowercase letters and a number.</small></label><button className="primary-button" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}<ArrowIcon /></button></form><p className="auth-switch">Already registered? <button className="inline-link" onClick={() => navigate('/professional/login')}>Professional sign in</button></p></AuthShell>
}
