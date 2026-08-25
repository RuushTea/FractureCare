package com.fracturecare.prediction;

import com.fracturecare.common.BadRequestException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

@Component
@ConditionalOnProperty(name = "app.ai.mode", havingValue = "mock", matchIfMissing = true)
public class MockInferenceClient implements InferenceClient {
    @Override
    public InferenceResult predict(Path imagePath) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(imagePath));
            int selector = Byte.toUnsignedInt(digest[0]) % 3;
            PredictionClass prediction = switch (selector) {
                case 0 -> PredictionClass.NO_FRACTURE;
                case 1 -> PredictionClass.ONE_FRACTURE;
                default -> PredictionClass.MULTIPLE_FRACTURES;
            };
            double confidence = 0.70 + (Byte.toUnsignedInt(digest[1]) / 255.0) * 0.24;
            return new InferenceResult(prediction, Math.min(confidence, 0.94), "mock-fracture-model-0.1", true);
        } catch (IOException exception) {
            throw new BadRequestException("The uploaded image could not be processed.", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Mock inference failed", exception);
        }
    }
}
