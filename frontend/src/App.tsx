import { useEffect } from 'react'
import { useAuth } from './auth/AuthContext'
import { navigate, useRoute } from './lib/route'
import { DashboardPage } from './pages/DashboardPage'
import { HistoryPage } from './pages/HistoryPage'
import { LandingPage } from './pages/LandingPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { ResultPage } from './pages/ResultPage'

export default function App() {
  const route = useRoute()
  const { token, ready } = useAuth()

  useEffect(() => {
    if (!ready) return
    const publicRoute = route.name === 'home' || route.name === 'login' || route.name === 'register'
    if (!token && !publicRoute) navigate('/')
    if (token && publicRoute) navigate('/dashboard')
  }, [ready, route.name, token])

  if (!ready) return <div className="app-loading"><span className="brand__mark"><span></span><span></span></span><span>Opening FractureCare...</span></div>
  if (!token) {
    if (route.name === 'home') return <LandingPage />
    return route.name === 'register' ? <RegisterPage /> : <LoginPage />
  }
  if (route.name === 'history') return <HistoryPage />
  if (route.name === 'result') return <ResultPage id={route.id} />
  return <DashboardPage />
}
