import { useEffect } from 'react'
import { useAuth } from './auth/AuthContext'
import { navigate, useRoute } from './lib/route'
import { DashboardPage } from './pages/DashboardPage'
import { HistoryPage } from './pages/HistoryPage'
import { LandingPage } from './pages/LandingPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { ResultPage } from './pages/ResultPage'
import { ProfessionalReviewsPage } from './pages/ProfessionalReviewsPage'
import { ProfessionalReviewPage } from './pages/ProfessionalReviewPage'
import { NotificationsPage } from './pages/NotificationsPage'
import { AdminProfessionalCreatePage } from './pages/AdminProfessionalCreatePage'

export default function App() {
  const route = useRoute()
  const { token, user, ready } = useAuth()

  useEffect(() => {
    if (!ready) return
    const publicRoute = route.name === 'home' || route.name === 'login' || route.name === 'register'
    if (!token && !publicRoute) navigate('/')
    if (token && publicRoute) navigate(user?.role === 'MEDICAL_PROFESSIONAL' ? '/professional/reviews' : '/dashboard')
    if (token && user?.role === 'USER' && ['professional-reviews', 'professional-review', 'admin-professional-create'].includes(route.name)) navigate('/dashboard')
    if (token && user?.role === 'MEDICAL_PROFESSIONAL' && route.name === 'admin-professional-create') navigate('/dashboard')
  }, [ready, route.name, token, user?.role])

  if (!ready) return <div className="app-loading"><span className="brand__mark"><span></span><span></span></span><span>Opening FractureCare...</span></div>
  if (!token) {
    if (route.name === 'home') return <LandingPage />
    return route.name === 'register' ? <RegisterPage /> : <LoginPage />
  }
  if (route.name === 'professional-reviews') return <ProfessionalReviewsPage />
  if (route.name === 'professional-review') return <ProfessionalReviewPage id={route.id} />
  if (route.name === 'admin-professional-create') return <AdminProfessionalCreatePage />
  if (route.name === 'notifications') return <NotificationsPage />
  if (route.name === 'history') return <HistoryPage />
  if (route.name === 'result') return <ResultPage id={route.id} />
  return <DashboardPage />
}
