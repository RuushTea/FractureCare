package com.fracturecare.explanation;

import com.fracturecare.prediction.PredictionClass;
import com.fracturecare.prediction.RiskCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ExplanationService {
    private static final Logger log = LoggerFactory.getLogger(ExplanationService.class);

    private final RuleBasedExplanationClient rules;
    private final GroqExplanationClient groq;

    public ExplanationService(RuleBasedExplanationClient rules, ObjectProvider<GroqExplanationClient> groqProvider) {
        this.rules = rules;
        this.groq = groqProvider.getIfAvailable();
    }

    public ExplanationResult explain(PredictionClass predictedClass, RiskCategory riskCategory,
                                     BigDecimal confidence, String predictionModelVersion) {
        if (groq == null || !groq.isConfigured()) {
            return rules.explain(predictedClass, riskCategory, confidence, predictionModelVersion);
        }
        try {
            return groq.explain(predictedClass, riskCategory, confidence, predictionModelVersion);
        } catch (RuntimeException exception) {
            log.warn("Groq explanation was unavailable; using the rule-based fallback: {}", exception.getMessage());
            return rules.explain(predictedClass, riskCategory, confidence, predictionModelVersion);
        }
    }
}
