package com.fracturecare.prediction;

import com.fracturecare.config.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "real", matchIfMissing = true)
public class HttpInferenceClient implements InferenceClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String endpoint;
    private final Duration timeout;

    public HttpInferenceClient(AppProperties properties, ObjectMapper objectMapper) {
        this.timeout = properties.ai().timeout() == null ? Duration.ofSeconds(30) : properties.ai().timeout();
        this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
        this.objectMapper = objectMapper;
        this.endpoint = properties.ai().baseUrl().replaceAll("/+$", "") + "/predict";
    }

    @Override
    public InferenceResult predict(Path imagePath) {
        try {
            String boundary = "----FractureCare" + UUID.randomUUID();
            byte[] body = multipart(imagePath, boundary);
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(timeout)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("AI service returned HTTP " + response.statusCode());
            }
            JsonNode json = objectMapper.readTree(response.body());
            String predicted = json.path("predictedClass").asText();
            double confidence = json.path("confidence").asDouble(-1);
            String modelVersion = json.path("modelVersion").asText("");
            if (predicted.isBlank() || confidence < 0 || modelVersion.isBlank()) throw new IllegalStateException("AI service returned an invalid prediction");
            return new InferenceResult(PredictionClass.valueOf(predicted), confidence, modelVersion, false);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("The AI service request was interrupted", exception);
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Could not reach the AI service", exception);
        }
    }

    private byte[] multipart(Path imagePath, String boundary) throws IOException {
        String contentType = imagePath.toString().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"image\"; filename=\"" + imagePath.getFileName() + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(Files.readAllBytes(imagePath));
        output.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }
}
