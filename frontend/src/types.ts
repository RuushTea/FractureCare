export type User = {
  id: number
  fullName: string
  email: string
  address?: string
  createdAt: string
}

export type AuthResponse = {
  token: string
  tokenType: 'Bearer'
  expiresInSeconds: number
  user: User
}

export type Prediction = {
  id: number
  originalFileName: string
  status: 'PROCESSING' | 'COMPLETED' | 'FAILED'
  predictedClass?: 'NO_FRACTURE' | 'ONE_FRACTURE' | 'MULTIPLE_FRACTURES'
  riskCategory?: 'NO_FRACTURE' | 'LOW_RISK' | 'HIGH_RISK'
  confidence?: number
  modelVersion?: string
  simulated: boolean
  explanation?: {
    summary: string
    confidenceMeaning: string
    nextStep: string
    questionsForClinician: string[]
    source: 'GROQ' | 'RULES'
    model: string
  }
  failureMessage?: string
  createdAt: string
  completedAt?: string
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export type ReportResponse = {
  id: number
  predictionId: number
  generatedAt: string
  downloadUrl: string
}
