package com.fracturecare.prediction;

import com.fracturecare.explanation.ExplanationSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.fracturecare.professionalreview.ProfessionalReviewDtos;

public final class PredictionDtos {
    private PredictionDtos() {}

    public record PredictionResponse(
            Long id,
            String originalFileName,
            PredictionStatus status,
            PredictionClass predictedClass,
            RiskCategory riskCategory,
            BigDecimal confidence,
            String modelVersion,
            boolean simulated,
            ExplanationResponse explanation,
            String failureMessage,
            Instant createdAt,
            Instant completedAt,
            ProfessionalReviewDtos.UserReviewState professionalReview
    ) {
        public static PredictionResponse from(Prediction prediction) {
            return from(prediction, null);
        }
        public static PredictionResponse from(Prediction prediction, ProfessionalReviewDtos.UserReviewState review) {
            return new PredictionResponse(prediction.getId(), prediction.getOriginalFileName(), prediction.getStatus(),
                    prediction.getPredictedClass(), prediction.getRiskCategory(), prediction.getConfidence(),
                    prediction.getModelVersion(), prediction.isSimulated(), ExplanationResponse.from(prediction), prediction.getFailureMessage(),
                    prediction.getCreatedAt(), prediction.getCompletedAt(), review);
        }
    }

    public record ExplanationResponse(
            String summary,
            String confidenceMeaning,
            String nextStep,
            List<String> questionsForClinician,
            ExplanationSource source,
            String model
    ) {
        public static ExplanationResponse from(Prediction prediction) {
            if (prediction.getExplanationSource() == null) return null;
            return new ExplanationResponse(prediction.getExplanationSummary(),
                    prediction.getExplanationConfidenceMeaning(), prediction.getExplanationNextStep(),
                    prediction.getExplanationQuestions(), prediction.getExplanationSource(),
                    prediction.getExplanationModel());
        }
    }
}
