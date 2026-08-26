package com.fracturecare.professionalreview;

import com.fracturecare.prediction.Prediction;
import com.fracturecare.user.UserAccount;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "professional_reviews")
public class ProfessionalReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "prediction_id", nullable = false, unique = true) private Prediction prediction;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewer_id") private UserAccount reviewer;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ProfessionalReviewStatus status;
    @Column(nullable = false, updatable = false) private Instant consentedAt;
    private Instant completedAt;
    private Boolean agreesWithAi;
    @Column(length = 2000) private String comment;
    @Version private long version;
    protected ProfessionalReview() {}
    public ProfessionalReview(Prediction prediction) { this.prediction = prediction; this.status = ProfessionalReviewStatus.PENDING; this.consentedAt = Instant.now(); }
    public void complete(UserAccount reviewer, boolean agreesWithAi, String comment) { this.reviewer = reviewer; this.agreesWithAi = agreesWithAi; this.comment = comment.trim(); this.status = ProfessionalReviewStatus.COMPLETED; this.completedAt = Instant.now(); }
    public Long getId() { return id; }
    public Prediction getPrediction() { return prediction; }
    public UserAccount getReviewer() { return reviewer; }
    public ProfessionalReviewStatus getStatus() { return status; }
    public Instant getConsentedAt() { return consentedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Boolean getAgreesWithAi() { return agreesWithAi; }
    public String getComment() { return comment; }
}
