package com.fracturecare.report;

import com.fracturecare.security.CurrentUser;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/predictions/{predictionId}/report")
    public ReportDtos.ReportResponse generate(@PathVariable Long predictionId, Authentication authentication) {
        return reportService.generate(CurrentUser.from(authentication).id(), predictionId);
    }

    @GetMapping("/reports/{reportId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long reportId, Authentication authentication) {
        var report = reportService.download(CurrentUser.from(authentication).id(), reportId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.fileName() + "\"")
                .body(report.resource());
    }
}
