package com.fracturecare.prediction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RiskCategoryServiceTest {
    private final RiskCategoryService service = new RiskCategoryService();

    @Test
    void mapsModelClassesToBoundedSystemCategories() {
        assertThat(service.categorize(PredictionClass.NO_FRACTURE)).isEqualTo(RiskCategory.NO_FRACTURE);
        assertThat(service.categorize(PredictionClass.ONE_FRACTURE)).isEqualTo(RiskCategory.LOW_RISK);
        assertThat(service.categorize(PredictionClass.MULTIPLE_FRACTURES)).isEqualTo(RiskCategory.HIGH_RISK);
    }
}
