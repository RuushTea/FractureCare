package com.fracturecare.explanation;

import com.fracturecare.prediction.PredictionClass;
import com.fracturecare.prediction.RiskCategory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class RuleBasedExplanationClient implements ExplanationClient {
    @Override
    public ExplanationResult explain(PredictionClass predictedClass, RiskCategory riskCategory,
                                     BigDecimal confidence, String predictionModelVersion) {
        String summary = switch (predictedClass) {
            case NO_FRACTURE -> "The image model placed this X-ray in the no-fracture-pattern category. Subtle fractures can still be missed, so this result does not rule out an injury.";
            case ONE_FRACTURE -> "The image model identified a pattern associated with one possible fracture. This is a model classification and requires professional confirmation.";
            case MULTIPLE_FRACTURES -> "The image model identified patterns associated with more than one possible fracture. Prompt professional assessment is recommended.";
        };
        String percentage = confidence.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP) + "%";
        String confidenceMeaning = percentage + " is how strongly the model preferred this category over its alternatives. It is not injury severity, recovery probability, or a guarantee that the result is correct.";
        String nextStep = switch (riskCategory) {
            case NO_FRACTURE -> "Arrange clinical review if pain, swelling, reduced movement, or other symptoms continue. Seek urgent care if symptoms are severe or worsening.";
            case LOW_RISK -> "Share the original X-ray with a qualified medical professional for confirmation and advice. Seek urgent care if symptoms are severe or worsening.";
            case HIGH_RISK -> "Arrange prompt assessment by a qualified medical professional. Seek urgent care for severe pain, deformity, numbness, heavy bleeding, or other emergency symptoms.";
        };
        return new ExplanationResult(summary, confidenceMeaning, nextStep,
                List.of("What does the original X-ray show?", "Is any additional imaging or follow-up needed?"),
                ExplanationSource.RULES, "fracturecare-rules-1.0");
    }
}
