import { useEffect, useState } from 'react'

export type Route =
  | { name: 'home' }
  | { name: 'login' }
  | { name: 'register' }
  | { name: 'professional-login' }
  | { name: 'professional-register' }
  | { name: 'dashboard' }
  | { name: 'history' }
  | { name: 'result'; id: number }
  | { name: 'notifications' }
  | { name: 'professional-reviews' }
  | { name: 'professional-review'; id: number }

function parseHash(): Route {
  const hash = window.location.hash.replace(/^#/, '') || '/'
  const result = hash.match(/^\/results\/(\d+)$/)
  if (result) return { name: 'result', id: Number(result[1]) }
  const professionalReview = hash.match(/^\/professional\/reviews\/(\d+)$/)
  if (professionalReview) return { name: 'professional-review', id: Number(professionalReview[1]) }
  if (hash === '/professional/reviews') return { name: 'professional-reviews' }
  if (hash === '/professional/login') return { name: 'professional-login' }
  if (hash === '/professional/register') return { name: 'professional-register' }
  if (hash === '/login') return { name: 'login' }
  if (hash === '/register') return { name: 'register' }
  if (hash === '/history') return { name: 'history' }
  if (hash === '/notifications') return { name: 'notifications' }
  if (hash === '/') return { name: 'home' }
  return { name: 'dashboard' }
}

export function navigate(path: string) {
  window.location.hash = path
}

export function useRoute() {
  const [route, setRoute] = useState<Route>(parseHash)
  useEffect(() => {
    const update = () => setRoute(parseHash())
    window.addEventListener('hashchange', update)
    return () => window.removeEventListener('hashchange', update)
  }, [])
  return route
}
