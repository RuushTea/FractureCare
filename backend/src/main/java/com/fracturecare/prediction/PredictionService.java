package com.fracturecare.prediction;

import com.fracturecare.common.NotFoundException;
import com.fracturecare.common.BadRequestException;
import com.fracturecare.explanation.ExplanationService;
import com.fracturecare.storage.FileStorageService;
import com.fracturecare.storage.StoredImage;
import com.fracturecare.user.UserAccount;
import com.fracturecare.user.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PredictionService {
    private final PredictionRepository predictions;
    private final UserRepository users;
    private final FileStorageService storage;
    private final InferenceClient inferenceClient;
    private final RiskCategoryService riskCategoryService;
    private final ExplanationService explanationService;

    public PredictionService(PredictionRepository predictions, UserRepository users, FileStorageService storage,
                             InferenceClient inferenceClient, RiskCategoryService riskCategoryService,
                             ExplanationService explanationService) {
        this.predictions = predictions;
        this.users = users;
        this.storage = storage;
        this.inferenceClient = inferenceClient;
        this.riskCategoryService = riskCategoryService;
        this.explanationService = explanationService;
    }

    @Transactional
    public PredictionDtos.PredictionResponse create(Long userId, MultipartFile file) {
        UserAccount user = users.findById(userId).orElseThrow(() -> new NotFoundException("User account was not found."));
        StoredImage image = storage.validateAndStore(file);
        Prediction prediction = predictions.save(new Prediction(user, image.reference(), image.originalFileName(), image.contentType()));
        try {
            InferenceResult result = inferenceClient.predict(image.path());
            RiskCategory riskCategory = riskCategoryService.categorize(result.predictedClass());
            prediction.complete(result, riskCategory);
        } catch (RuntimeException exception) {
            prediction.fail("The prediction service could not complete this request. Please try again later.");
        }
        return PredictionDtos.PredictionResponse.from(predictions.save(prediction));
    }

    @Transactional
    public PredictionDtos.PredictionResponse explain(Long userId, Long predictionId) {
        Prediction prediction = requireOwned(userId, predictionId);
        if (prediction.getStatus() != PredictionStatus.COMPLETED) {
            throw new BadRequestException("Only a completed prediction can be explained.");
        }
        prediction.addExplanation(explanationService.explain(prediction.getPredictedClass(),
                prediction.getRiskCategory(), prediction.getConfidence(), prediction.getModelVersion()));
        return PredictionDtos.PredictionResponse.from(predictions.save(prediction));
    }

    @Transactional(readOnly = true)
    public Page<PredictionDtos.PredictionResponse> history(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.unsorted());
        return predictions.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(PredictionDtos.PredictionResponse::from);
    }

    @Transactional(readOnly = true)
    public PredictionDtos.PredictionResponse get(Long userId, Long predictionId) {
        return PredictionDtos.PredictionResponse.from(requireOwned(userId, predictionId));
    }

    @Transactional(readOnly = true)
    public ImageDownload image(Long userId, Long predictionId) {
        Prediction prediction = requireOwned(userId, predictionId);
        return new ImageDownload(storage.load(prediction.getImageReference()), prediction.getImageContentType());
    }

    @Transactional(readOnly = true)
    public Prediction requireOwned(Long userId, Long predictionId) {
        return predictions.findByIdAndUserId(predictionId, userId)
                .orElseThrow(() -> new NotFoundException("Prediction was not found."));
    }

    public record ImageDownload(Resource resource, String contentType) {}
}
