package com.fracturecare.professionalreview;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProfessionalReviewRepository extends JpaRepository<ProfessionalReview, Long> {
    Optional<ProfessionalReview> findByPredictionId(Long predictionId);
    List<ProfessionalReview> findByStatusOrderByConsentedAtAsc(ProfessionalReviewStatus status);
}
