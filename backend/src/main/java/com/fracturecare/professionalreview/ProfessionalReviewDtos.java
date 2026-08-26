package com.fracturecare.professionalreview;

import com.fracturecare.prediction.Prediction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class ProfessionalReviewDtos {
    private ProfessionalReviewDtos() {}
    public record CompleteRequest(@NotNull Boolean agreesWithAi, @NotBlank @Size(max = 2000) String comment) {}
    public record ReviewSummary(Long reviewId, Long predictionId, String predictionReference, Instant dateRequested, Object predictedClass, Object riskCategory, Object confidence, String modelVersion, ProfessionalReviewStatus status) {}
    public record ReviewDetail(Long reviewId, Long predictionId, String predictionReference, String originalFileName, Object predictedClass, Object riskCategory, Object confidence, String modelVersion, Instant createdAt, Object explanation, ProfessionalReviewStatus status, Instant consentedAt, Instant completedAt, Boolean agreesWithAi, String comment, String reviewerName) {
        public static ReviewDetail from(ProfessionalReview review, Object explanation) {
            Prediction p = review.getPrediction();
            return new ReviewDetail(review.getId(), p.getId(), "FC-" + String.format("%06d", p.getId()), p.getOriginalFileName(), p.getPredictedClass(), p.getRiskCategory(), p.getConfidence(), p.getModelVersion(), p.getCreatedAt(), explanation, review.getStatus(), review.getConsentedAt(), review.getCompletedAt(), review.getAgreesWithAi(), review.getComment(), review.getReviewer() == null ? null : review.getReviewer().getFullName());
        }
    }
    public record UserReviewState(ProfessionalReviewStatus status, Instant consentedAt, Instant completedAt, Boolean agreesWithAi, String comment, String reviewerName) {}
}
