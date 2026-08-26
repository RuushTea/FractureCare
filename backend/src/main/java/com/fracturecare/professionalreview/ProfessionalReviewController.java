package com.fracturecare.professionalreview;

import com.fracturecare.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class ProfessionalReviewController {
    private final ProfessionalReviewService service;
    public ProfessionalReviewController(ProfessionalReviewService service) { this.service = service; }

    @PostMapping("/api/predictions/{predictionId}/professional-review")
    public ProfessionalReviewDtos.UserReviewState request(@PathVariable Long predictionId, Authentication authentication) { return service.request(CurrentUser.from(authentication).id(), predictionId); }
    @GetMapping("/api/professional/reviews")
    public List<ProfessionalReviewDtos.ReviewSummary> pending() { return service.pending(); }
    @GetMapping("/api/professional/reviews/{reviewId}")
    public ProfessionalReviewDtos.ReviewDetail detail(@PathVariable Long reviewId) { return service.detail(reviewId); }
    @GetMapping("/api/professional/reviews/{reviewId}/image")
    public ResponseEntity<Resource> image(@PathVariable Long reviewId) {
        var download = service.image(reviewId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(download.contentType())).body(download.resource());
    }
    @PostMapping("/api/professional/reviews/{reviewId}/complete")
    public ProfessionalReviewDtos.ReviewDetail complete(@PathVariable Long reviewId, @Valid @RequestBody ProfessionalReviewDtos.CompleteRequest request, Authentication authentication) { return service.complete(reviewId, CurrentUser.from(authentication).id(), request); }
}
