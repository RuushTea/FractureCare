package com.fracturecare.notification;
import java.time.Instant;
public final class NotificationDtos {
    private NotificationDtos() {}
    public record NotificationResponse(Long id, NotificationType type, Long predictionId, String title, String message, boolean read, Instant createdAt) { public static NotificationResponse from(Notification n) { return new NotificationResponse(n.getId(), n.getType(), n.getPrediction() == null ? null : n.getPrediction().getId(), n.getTitle(), n.getMessage(), n.isRead(), n.getCreatedAt()); } }
    public record UnreadCount(long count) {}
}
