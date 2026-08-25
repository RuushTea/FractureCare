package com.fracturecare.prediction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    Page<Prediction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Prediction> findByIdAndUserId(Long id, Long userId);
}
