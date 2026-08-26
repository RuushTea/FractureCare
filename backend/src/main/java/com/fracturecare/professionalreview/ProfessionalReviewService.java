package com.fracturecare.professionalreview;

import com.fracturecare.common.BadRequestException;
import com.fracturecare.common.ConflictException;
import com.fracturecare.common.NotFoundException;
import com.fracturecare.notification.NotificationService;
import com.fracturecare.prediction.Prediction;
import com.fracturecare.prediction.PredictionDtos;
import com.fracturecare.prediction.PredictionRepository;
import com.fracturecare.prediction.PredictionStatus;
import com.fracturecare.user.AccountRole;
import com.fracturecare.user.UserAccount;
import com.fracturecare.user.UserRepository;
import com.fracturecare.storage.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProfessionalReviewService {
    private final ProfessionalReviewRepository reviews;
    private final PredictionRepository predictions;
    private final UserRepository users;
    private final NotificationService notifications;
    private final FileStorageService storage;

    public ProfessionalReviewService(ProfessionalReviewRepository reviews, PredictionRepository predictions, UserRepository users, NotificationService notifications, FileStorageService storage) {
        this.reviews = reviews; this.predictions = predictions; this.users = users; this.notifications = notifications; this.storage = storage;
    }

    @Transactional
    public ProfessionalReviewDtos.UserReviewState request(Long userId, Long predictionId) {
        UserAccount owner = users.findById(userId).orElseThrow(() -> new NotFoundException("User account was not found."));
        if (owner.getRole() != AccountRole.USER) throw new BadRequestException("Only normal users can request professional review.");
        Prediction prediction = predictions.findByIdAndUserId(predictionId, userId).orElseThrow(() -> new NotFoundException("Prediction was not found."));
        if (prediction.getStatus() != PredictionStatus.COMPLETED) throw new BadRequestException("Only a completed prediction can be sent for professional review.");
        if (reviews.findByPredictionId(predictionId).isPresent()) throw new ConflictException("A professional review has already been requested.");
        ProfessionalReview saved = reviews.save(new ProfessionalReview(prediction));
        return state(saved);
    }

    @Transactional(readOnly = true)
    public List<ProfessionalReviewDtos.ReviewSummary> pending() {
        return reviews.findByStatusOrderByConsentedAtAsc(ProfessionalReviewStatus.PENDING).stream().map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public ProfessionalReviewDtos.ReviewDetail detail(Long reviewId) {
        ProfessionalReview review = find(reviewId);
        return ProfessionalReviewDtos.ReviewDetail.from(review, PredictionDtos.ExplanationResponse.from(review.getPrediction()));
    }

    @Transactional(readOnly = true)
    public ImageDownload image(Long reviewId) {
        ProfessionalReview review = find(reviewId);
        return new ImageDownload(storage.load(review.getPrediction().getImageReference()), review.getPrediction().getImageContentType());
    }

    @Transactional
    public ProfessionalReviewDtos.ReviewDetail complete(Long reviewId, Long professionalId, ProfessionalReviewDtos.CompleteRequest request) {
        UserAccount professional = users.findById(professionalId).orElseThrow(() -> new NotFoundException("Professional account was not found."));
        if (professional.getRole() != AccountRole.MEDICAL_PROFESSIONAL) throw new ConflictException("Only medical professionals can complete reviews.");
        ProfessionalReview review = find(reviewId);
        if (review.getStatus() != ProfessionalReviewStatus.PENDING) throw new ConflictException("This review has already been completed.");
        review.complete(professional, request.agreesWithAi(), request.comment());
        ProfessionalReview saved = reviews.save(review);
        notifications.createReviewCompleted(saved.getPrediction().getUser(), saved.getPrediction());
        return ProfessionalReviewDtos.ReviewDetail.from(saved, PredictionDtos.ExplanationResponse.from(saved.getPrediction()));
    }

    @Transactional(readOnly = true)
    public ProfessionalReviewDtos.UserReviewState stateFor(Long userId, Long predictionId) {
        return reviews.findByPredictionId(predictionId).filter(r -> r.getPrediction().getUser().getId().equals(userId)).map(this::state).orElse(null);
    }

    private ProfessionalReview find(Long id) { return reviews.findById(id).orElseThrow(() -> new NotFoundException("Professional review was not found.")); }
    private ProfessionalReviewDtos.UserReviewState state(ProfessionalReview r) { return new ProfessionalReviewDtos.UserReviewState(r.getStatus(), r.getConsentedAt(), r.getCompletedAt(), r.getAgreesWithAi(), r.getComment(), r.getReviewer() == null ? null : r.getReviewer().getFullName()); }
    private ProfessionalReviewDtos.ReviewSummary summary(ProfessionalReview r) { Prediction p = r.getPrediction(); return new ProfessionalReviewDtos.ReviewSummary(r.getId(), p.getId(), "FC-" + String.format("%06d", p.getId()), r.getConsentedAt(), p.getPredictedClass(), p.getRiskCategory(), p.getConfidence(), p.getModelVersion(), r.getStatus()); }
    public record ImageDownload(Resource resource, String contentType) {}
}
