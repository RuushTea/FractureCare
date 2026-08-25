package com.fracturecare.prediction;

import org.springframework.stereotype.Service;

@Service
public class RiskCategoryService {
    public RiskCategory categorize(PredictionClass predictionClass) {
        return switch (predictionClass) {
            case NO_FRACTURE -> RiskCategory.NO_FRACTURE;
            case ONE_FRACTURE -> RiskCategory.LOW_RISK;
            case MULTIPLE_FRACTURES -> RiskCategory.HIGH_RISK;
        };
    }
}
