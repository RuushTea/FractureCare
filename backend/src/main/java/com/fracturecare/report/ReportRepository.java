package com.fracturecare.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByPredictionId(Long predictionId);
    Optional<Report> findByIdAndPredictionUserId(Long id, Long userId);
}
