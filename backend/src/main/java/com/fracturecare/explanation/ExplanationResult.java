package com.fracturecare.explanation;

import java.util.List;

public record ExplanationResult(
        String summary,
        String confidenceMeaning,
        String nextStep,
        List<String> questionsForClinician,
        ExplanationSource source,
        String model
) {
    public ExplanationResult {
        questionsForClinician = List.copyOf(questionsForClinician);
    }
}
