import type { ReactNode } from 'react'
import { useAuth } from '../auth/AuthContext'
import { navigate } from '../lib/route'
import { HistoryIcon, ShieldIcon, UploadIcon } from './Icons'

export function Logo() {
  const { token } = useAuth()
  return (
    <button className="brand" type="button" onClick={() => navigate(token ? '/dashboard' : '/')} aria-label="FractureCare home">
      <span className="brand__mark"><span></span><span></span></span>
      <span>Fracture<span>Care</span></span>
    </button>
  )
}

export function AppShell({ active, children }: { active: 'dashboard' | 'history' | 'result'; children: ReactNode }) {
  const { user, logout } = useAuth()
  return (
    <div className="app-shell">
      <header className="topbar">
        <Logo />
        <nav aria-label="Main navigation">
          <button className={active === 'dashboard' ? 'nav-link active' : 'nav-link'} onClick={() => navigate('/dashboard')}><UploadIcon />New analysis</button>
          <button className={active === 'history' ? 'nav-link active' : 'nav-link'} onClick={() => navigate('/history')}><HistoryIcon />History</button>
        </nav>
        <div className="account-menu">
          <span className="avatar" aria-hidden="true">{user?.fullName?.charAt(0).toUpperCase() ?? 'U'}</span>
          <div><strong>{user?.fullName}</strong><span>{user?.email}</span></div>
          <button className="text-button" onClick={() => { logout(); navigate('/') }}>Sign out</button>
        </div>
      </header>
      <main>{children}</main>
      <footer className="site-footer"><ShieldIcon /> FractureCare provides educational second-opinion support. It does not provide a medical diagnosis.</footer>
    </div>
  )
}

export function AuthShell({ children, heading, intro }: { children: ReactNode; heading: string; intro: string }) {
  return (
    <main className="auth-page">
      <section className="auth-story">
        <Logo />
        <div className="auth-story__content">
          <span className="eyebrow">FractureCare</span>
          <h1>AI-Fracture Detection as a Second Opinion</h1>
          <p>Sign in to review an X-ray result, revisit earlier analyses and download your reports.</p>
          <div className="trust-row"><ShieldIcon /><span><strong>Private by design</strong>Protected accounts, files and reports</span></div>
        </div>
        <p className="auth-disclaimer">Not for diagnosis, treatment decisions or emergency triage.</p>
      </section>
      <section className="auth-panel">
        <div className="auth-card">
          <span className="eyebrow">Welcome to FractureCare</span>
          <h2>{heading}</h2>
          <p>{intro}</p>
          {children}
        </div>
      </section>
    </main>
  )
}
