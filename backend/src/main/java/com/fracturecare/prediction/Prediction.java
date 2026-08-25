package com.fracturecare.prediction;

import com.fracturecare.explanation.ExplanationResult;
import com.fracturecare.explanation.ExplanationSource;
import com.fracturecare.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "predictions")
public class Prediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, unique = true, length = 100)
    private String imageReference;

    @Column(nullable = false, length = 100)
    private String originalFileName;

    @Column(nullable = false, length = 50)
    private String imageContentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PredictionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PredictionClass predictedClass;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RiskCategory riskCategory;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(length = 80)
    private String modelVersion;

    @Column(nullable = false)
    private boolean simulated;

    @Column(length = 500)
    private String failureMessage;

    @Column(length = 1200)
    private String explanationSummary;

    @Column(length = 1200)
    private String explanationConfidenceMeaning;

    @Column(length = 1200)
    private String explanationNextStep;

    @Column(length = 1000)
    private String explanationQuestions;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ExplanationSource explanationSource;

    @Column(length = 120)
    private String explanationModel;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant completedAt;

    @Version
    private long version;

    protected Prediction() {}

    public Prediction(UserAccount user, String imageReference, String originalFileName, String imageContentType) {
        this.user = user;
        this.imageReference = imageReference;
        this.originalFileName = originalFileName;
        this.imageContentType = imageContentType;
        this.status = PredictionStatus.PROCESSING;
        this.createdAt = Instant.now();
    }

    public void complete(InferenceResult result, RiskCategory riskCategory) {
        this.status = PredictionStatus.COMPLETED;
        this.predictedClass = result.predictedClass();
        this.riskCategory = riskCategory;
        this.confidence = BigDecimal.valueOf(result.confidence());
        this.modelVersion = result.modelVersion();
        this.simulated = result.simulated();
        this.completedAt = Instant.now();
        this.failureMessage = null;
    }

    public void fail(String message) {
        this.status = PredictionStatus.FAILED;
        this.failureMessage = message;
        this.completedAt = Instant.now();
    }

    public void addExplanation(ExplanationResult explanation) {
        this.explanationSummary = explanation.summary();
        this.explanationConfidenceMeaning = explanation.confidenceMeaning();
        this.explanationNextStep = explanation.nextStep();
        this.explanationQuestions = String.join("\n", explanation.questionsForClinician());
        this.explanationSource = explanation.source();
        this.explanationModel = explanation.model();
    }

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public String getImageReference() { return imageReference; }
    public String getOriginalFileName() { return originalFileName; }
    public String getImageContentType() { return imageContentType; }
    public PredictionStatus getStatus() { return status; }
    public PredictionClass getPredictedClass() { return predictedClass; }
    public RiskCategory getRiskCategory() { return riskCategory; }
    public BigDecimal getConfidence() { return confidence; }
    public String getModelVersion() { return modelVersion; }
    public boolean isSimulated() { return simulated; }
    public String getFailureMessage() { return failureMessage; }
    public String getExplanationSummary() { return explanationSummary; }
    public String getExplanationConfidenceMeaning() { return explanationConfidenceMeaning; }
    public String getExplanationNextStep() { return explanationNextStep; }
    public List<String> getExplanationQuestions() {
        return explanationQuestions == null || explanationQuestions.isBlank()
                ? List.of()
                : explanationQuestions.lines().filter(line -> !line.isBlank()).toList();
    }
    public ExplanationSource getExplanationSource() { return explanationSource; }
    public String getExplanationModel() { return explanationModel; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}
