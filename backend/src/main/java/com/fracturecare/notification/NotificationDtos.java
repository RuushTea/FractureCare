package com.fracturecare.notification;
import java.time.Instant;
public final class NotificationDtos {
    private NotificationDtos() {}
    public record NotificationResponse(Long id, NotificationType type, Long predictionId, String predictionReference, String predictedClass, String riskCategory, String title, String message, boolean read, Instant createdAt) {
        public static NotificationResponse from(Notification n) {
            var prediction = n.getPrediction();
            Long predictionId = prediction == null ? null : prediction.getId();
            return new NotificationResponse(
                    n.getId(),
                    n.getType(),
                    predictionId,
                    predictionId == null ? null : "FC-" + String.format("%06d", predictionId),
                    prediction == null || prediction.getPredictedClass() == null ? null : prediction.getPredictedClass().name(),
                    prediction == null || prediction.getRiskCategory() == null ? null : prediction.getRiskCategory().name(),
                    n.getTitle(),
                    n.getMessage(),
                    n.isRead(),
                    n.getCreatedAt()
            );
        }
    }
    public record UnreadCount(long count) {}
}

