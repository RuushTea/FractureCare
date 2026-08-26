import type { AuthResponse, Notification, PageResponse, Prediction, ReportResponse, ReviewDetail, ReviewSummary, User } from '../types'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(message: string, status: number, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

async function request<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const headers = new Headers(options.headers)
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json')

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers })
  if (!response.ok) {
    const problem = await response.json().catch(() => ({})) as { detail?: string; errors?: Record<string, string> }
    throw new ApiError(problem.detail ?? 'The request could not be completed.', response.status, problem.errors)
  }
  return response.json() as Promise<T>
}

export const api = {
  register: (body: { fullName: string; email: string; address: string; password: string }) =>
    request<AuthResponse>('/api/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  login: (body: { email: string; password: string }) =>
    request<AuthResponse>('/api/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  professionalRegister: (body: { fullName: string; email: string; username: string; password: string }) => request<AuthResponse>('/api/auth/professional/register', { method: 'POST', body: JSON.stringify(body) }),
  professionalLogin: (body: { username: string; password: string }) => request<AuthResponse>('/api/auth/professional/login', { method: 'POST', body: JSON.stringify(body) }),
  me: (token: string) => request<User>('/api/auth/me', {}, token),
  createPrediction: (image: File, token: string) => {
    const form = new FormData()
    form.append('image', image)
    return request<Prediction>('/api/predictions', { method: 'POST', body: form }, token)
  },
  getPrediction: (id: number, token: string) => request<Prediction>(`/api/predictions/${id}`, {}, token),
  explainPrediction: (id: number, token: string) =>
    request<Prediction>(`/api/predictions/${id}/explanation`, { method: 'POST' }, token),
  requestProfessionalReview: (id: number, token: string) => request<Prediction['professionalReview']>(`/api/predictions/${id}/professional-review`, { method: 'POST' }, token),
  history: (page: number, token: string) => request<PageResponse<Prediction>>(`/api/predictions?page=${page}&size=8`, {}, token),
  createReport: (predictionId: number, token: string) =>
    request<ReportResponse>(`/api/predictions/${predictionId}/report`, { method: 'POST' }, token),
  downloadReport: async (report: ReportResponse, token: string) => {
    const response = await fetch(`${API_BASE}${report.downloadUrl}`, { headers: { Authorization: `Bearer ${token}` } })
    if (!response.ok) throw new ApiError('The report could not be downloaded.', response.status)
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `fracturecare-report-${report.predictionId}.pdf`
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(url)
  },
  notifications: (token: string) => request<Notification[]>('/api/notifications', {}, token),
  unreadNotifications: (token: string) => request<{ count: number }>('/api/notifications/unread-count', {}, token),
  markNotificationRead: (id: number, token: string) => request<Notification>(`/api/notifications/${id}/read`, { method: 'PATCH' }, token),
  professionalReviews: (token: string) => request<ReviewSummary[]>('/api/professional/reviews', {}, token),
  professionalReview: (id: number, token: string) => request<ReviewDetail>(`/api/professional/reviews/${id}`, {}, token),
  professionalReviewImageUrl: (id: number) => `${API_BASE}/api/professional/reviews/${id}/image`,
  completeProfessionalReview: (id: number, body: { agreesWithAi: boolean; comment: string }, token: string) => request<ReviewDetail>(`/api/professional/reviews/${id}/complete`, { method: 'POST', body: JSON.stringify(body) }, token),
}
