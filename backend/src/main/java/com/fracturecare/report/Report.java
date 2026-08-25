package com.fracturecare.report;

import com.fracturecare.prediction.Prediction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prediction_id", nullable = false, unique = true)
    private Prediction prediction;

    @Column(nullable = false, unique = true, length = 100)
    private String fileReference;

    @Column(nullable = false, updatable = false)
    private Instant generatedAt;

    protected Report() {}

    public Report(Prediction prediction, String fileReference) {
        this.prediction = prediction;
        this.fileReference = fileReference;
        this.generatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Prediction getPrediction() { return prediction; }
    public String getFileReference() { return fileReference; }
    public Instant getGeneratedAt() { return generatedAt; }
}
