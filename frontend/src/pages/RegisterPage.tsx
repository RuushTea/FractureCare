import { useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { Alert } from '../components/Alert'
import { AuthShell } from '../components/AppShell'
import { ArrowIcon } from '../components/Icons'
import { ApiError, api } from '../lib/api'
import { navigate } from '../lib/route'

type Fields = { fullName: string; email: string; address: string; password: string }
const initial: Fields = { fullName: '', email: '', address: '', password: '' }

export function RegisterPage() {
  const { accept } = useAuth()
  const [fields, setFields] = useState(initial)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  function update(name: keyof Fields, value: string) {
    setFields(current => ({ ...current, [name]: value }))
    setFieldErrors(current => ({ ...current, [name]: '' }))
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError('')
    setBusy(true)
    try {
      accept(await api.register(fields))
      navigate('/dashboard')
    } catch (exception) {
      if (exception instanceof ApiError) {
        setError(exception.message)
        setFieldErrors(exception.fieldErrors)
      } else setError('Your account could not be created.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <AuthShell heading="Create your account" intro="Your account keeps uploaded images, results and reports private to you.">
      {error && <Alert>{error}</Alert>}
      <form className="form-stack" onSubmit={submit}>
        <label>Full name<input autoComplete="name" required value={fields.fullName} onChange={event => update('fullName', event.target.value)} /><small className="field-error">{fieldErrors.fullName}</small></label>
        <label>Email address<input type="email" autoComplete="email" required value={fields.email} onChange={event => update('email', event.target.value)} /><small className="field-error">{fieldErrors.email}</small></label>
        <label>Address <span className="optional">Optional</span><input autoComplete="street-address" value={fields.address} onChange={event => update('address', event.target.value)} /><small className="field-error">{fieldErrors.address}</small></label>
        <label>Password<input type="password" autoComplete="new-password" minLength={10} required value={fields.password} onChange={event => update('password', event.target.value)} /><small>At least 10 characters with upper and lowercase letters and a number.</small><small className="field-error">{fieldErrors.password}</small></label>
        <button className="primary-button" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}<ArrowIcon /></button>
      </form>
      <p className="auth-switch">Already have an account? <button className="inline-link" onClick={() => navigate('/login')}>Sign in</button></p>
    </AuthShell>
  )
}
