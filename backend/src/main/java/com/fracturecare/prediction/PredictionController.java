package com.fracturecare.prediction;

import com.fracturecare.security.CurrentUser;
import com.fracturecare.common.PageResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {
    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PredictionDtos.PredictionResponse create(@RequestParam("image") MultipartFile image, Authentication authentication) {
        return predictionService.create(CurrentUser.from(authentication).id(), image);
    }

    @GetMapping
    public PageResponse<PredictionDtos.PredictionResponse> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return PageResponse.from(predictionService.history(CurrentUser.from(authentication).id(), page, size));
    }

    @GetMapping("/{predictionId}")
    public PredictionDtos.PredictionResponse get(@PathVariable Long predictionId, Authentication authentication) {
        return predictionService.get(CurrentUser.from(authentication).id(), predictionId);
    }

    @PostMapping("/{predictionId}/explanation")
    public PredictionDtos.PredictionResponse explain(@PathVariable Long predictionId, Authentication authentication) {
        return predictionService.explain(CurrentUser.from(authentication).id(), predictionId);
    }

    @GetMapping("/{predictionId}/image")
    public ResponseEntity<Resource> image(@PathVariable Long predictionId, Authentication authentication) {
        var download = predictionService.image(CurrentUser.from(authentication).id(), predictionId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(download.contentType())).body(download.resource());
    }
}
