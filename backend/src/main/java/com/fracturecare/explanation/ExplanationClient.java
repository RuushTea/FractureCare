package com.fracturecare.explanation;

import com.fracturecare.prediction.PredictionClass;
import com.fracturecare.prediction.RiskCategory;

import java.math.BigDecimal;

public interface ExplanationClient {
    ExplanationResult explain(PredictionClass predictedClass, RiskCategory riskCategory,
                              BigDecimal confidence, String predictionModelVersion);
}
