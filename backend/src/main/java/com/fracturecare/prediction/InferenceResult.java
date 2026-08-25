package com.fracturecare.prediction;

public record InferenceResult(
        PredictionClass predictedClass,
        double confidence,
        String modelVersion,
        boolean simulated
) {}
