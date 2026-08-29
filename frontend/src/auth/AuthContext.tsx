import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api } from '../lib/api'
import type { AuthResponse, User } from '../types'

type AuthContextValue = {
  token: string | null
  user: User | null
  ready: boolean
  unreadCount: number
  accept: (response: AuthResponse) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)
const TOKEN_KEY = 'fracturecare-token'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => sessionStorage.getItem(TOKEN_KEY))
  const [user, setUser] = useState<User | null>(null)
  const [ready, setReady] = useState(false)
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    if (!token) {
      setReady(true)
      return
    }
    api.me(token)
      .then(setUser)
      .catch(() => {
        sessionStorage.removeItem(TOKEN_KEY)
        setToken(null)
      })
      .finally(() => setReady(true))
  }, [token])

  useEffect(() => {
    if (!token || !user) { setUnreadCount(0); return }
    const refresh = () => api.unreadNotifications(token).then(result => setUnreadCount(result.count)).catch(() => undefined)
    refresh()
    const timer = window.setInterval(refresh, 45_000)
    return () => window.clearInterval(timer)
  }, [token, user])

  const value = useMemo<AuthContextValue>(() => ({
    token,
    user,
    ready,
    unreadCount,
    accept: response => {
      sessionStorage.setItem(TOKEN_KEY, response.token)
      setToken(response.token)
      setUser(response.user)
    },
    logout: () => {
      sessionStorage.removeItem(TOKEN_KEY)
      setToken(null)
      setUser(null)
    },
  }), [token, user, ready])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
