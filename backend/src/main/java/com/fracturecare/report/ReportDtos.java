package com.fracturecare.report;

import java.time.Instant;

public final class ReportDtos {
    private ReportDtos() {}

    public record ReportResponse(Long id, Long predictionId, Instant generatedAt, String downloadUrl) {
        public static ReportResponse from(Report report) {
            return new ReportResponse(report.getId(), report.getPrediction().getId(), report.getGeneratedAt(),
                    "/api/reports/" + report.getId() + "/download");
        }
    }
}
