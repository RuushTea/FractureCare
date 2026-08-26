import { useEffect } from 'react'
import { useAuth } from './auth/AuthContext'
import { navigate, useRoute } from './lib/route'
import { DashboardPage } from './pages/DashboardPage'
import { HistoryPage } from './pages/HistoryPage'
import { LandingPage } from './pages/LandingPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { ResultPage } from './pages/ResultPage'
import { ProfessionalLoginPage } from './pages/ProfessionalLoginPage'
import { ProfessionalRegisterPage } from './pages/ProfessionalRegisterPage'
import { ProfessionalReviewsPage } from './pages/ProfessionalReviewsPage'
import { ProfessionalReviewPage } from './pages/ProfessionalReviewPage'
import { NotificationsPage } from './pages/NotificationsPage'

export default function App() {
  const route = useRoute()
  const { token, user, ready } = useAuth()

  useEffect(() => {
    if (!ready) return
    const publicRoute = route.name === 'home' || route.name === 'login' || route.name === 'register' || route.name === 'professional-login' || route.name === 'professional-register'
    if (!token && !publicRoute) navigate('/')
    if (token && publicRoute) navigate(user?.role === 'MEDICAL_PROFESSIONAL' ? '/professional/reviews' : '/dashboard')
    if (token && user?.role === 'MEDICAL_PROFESSIONAL' && ['dashboard', 'history', 'notifications', 'result'].includes(route.name)) navigate('/professional/reviews')
    if (token && user?.role === 'USER' && ['professional-reviews', 'professional-review'].includes(route.name)) navigate('/dashboard')
  }, [ready, route.name, token, user?.role])

  if (!ready) return <div className="app-loading"><span className="brand__mark"><span></span><span></span></span><span>Opening FractureCare...</span></div>
  if (!token) {
    if (route.name === 'home') return <LandingPage />
    if (route.name === 'professional-login') return <ProfessionalLoginPage />
    if (route.name === 'professional-register') return <ProfessionalRegisterPage />
    return route.name === 'register' ? <RegisterPage /> : <LoginPage />
  }
  if (route.name === 'professional-reviews') return <ProfessionalReviewsPage />
  if (route.name === 'professional-review') return <ProfessionalReviewPage id={route.id} />
  if (route.name === 'notifications') return <NotificationsPage />
  if (route.name === 'history') return <HistoryPage />
  if (route.name === 'result') return <ResultPage id={route.id} />
  return <DashboardPage />
}
