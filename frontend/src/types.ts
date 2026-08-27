export type User = {
  id: number
  fullName: string
  email: string
  username?: string
  role: 'USER' | 'MEDICAL_PROFESSIONAL'
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
  professionalReview?: ProfessionalReviewState
}

export type ProfessionalReviewState = { status: 'PENDING' | 'COMPLETED'; consentedAt: string; completedAt?: string; agreesWithAi?: boolean; comment?: string; reviewerName?: string }
export type ReviewSummary = { reviewId: number; predictionId: number; predictionReference: string; dateRequested: string; predictedClass: string; riskCategory: string; confidence: number; modelVersion: string; status: 'PENDING' | 'COMPLETED' }
export type ReviewDetail = ReviewSummary & { originalFileName: string; createdAt: string; explanation?: Prediction['explanation']; consentedAt: string; completedAt?: string; agreesWithAi?: boolean; comment?: string; reviewerName?: string }
export type Notification = { id: number; type: 'PROFESSIONAL_REVIEW_COMPLETED'; predictionId?: number; predictionReference?: string; predictedClass?: Prediction['predictedClass']; riskCategory?: Prediction['riskCategory']; title: string; message: string; read: boolean; createdAt: string }

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

