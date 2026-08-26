package com.fracturecare.notification;
import com.fracturecare.prediction.Prediction;
import com.fracturecare.user.UserAccount;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private UserAccount user;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50) private NotificationType type;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "prediction_id") private Prediction prediction;
    @Column(nullable = false, length = 180) private String title;
    @Column(nullable = false, length = 500) private String message;
    @Column(name = "read_flag", nullable = false) private boolean read;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    protected Notification() {}
    public Notification(UserAccount user, Prediction prediction) { this.user = user; this.prediction = prediction; this.type = NotificationType.PROFESSIONAL_REVIEW_COMPLETED; this.title = "Medical professional review completed"; this.message = "A medical professional has reviewed your fracture analysis."; this.createdAt = Instant.now(); }
    public void markRead() { read = true; }
    public Long getId() { return id; } public NotificationType getType() { return type; } public Prediction getPrediction() { return prediction; } public String getTitle() { return title; } public String getMessage() { return message; } public boolean isRead() { return read; } public Instant getCreatedAt() { return createdAt; }
}
