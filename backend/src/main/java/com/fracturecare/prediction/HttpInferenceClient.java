package com.fracturecare.prediction;

import com.fracturecare.config.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "real", matchIfMissing = true)
public class HttpInferenceClient implements InferenceClient {
    private final ObjectMapper objectMapper;
    private final String endpoint;
    private final Duration timeout;

    public HttpInferenceClient(AppProperties properties, ObjectMapper objectMapper) {
        this.timeout = properties.ai().timeout() == null ? Duration.ofSeconds(30) : properties.ai().timeout();
        this.objectMapper = objectMapper;
        this.endpoint = properties.ai().baseUrl().replaceAll("/+$", "") + "/predict";
    }

    @Override
    public InferenceResult predict(Path imagePath) {
        try {
            String boundary = "FractureCareBoundary" + UUID.randomUUID().toString().replace("-", "");
            byte[] requestBody = multipartBody(imagePath, boundary);
            HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
            try {
                int timeoutMillis = Math.toIntExact(timeout.toMillis());
                connection.setConnectTimeout(timeoutMillis);
                connection.setReadTimeout(timeoutMillis);
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setFixedLengthStreamingMode(requestBody.length);
                try (var output = connection.getOutputStream()) {
                    output.write(requestBody);
                }
                int status = connection.getResponseCode();
                var responseStream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
                String responseBody = responseStream == null ? "" : new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);
                if (status < 200 || status >= 300) {
                    throw new IllegalStateException("AI service returned HTTP " + status + ": " + responseBody);
                }
                JsonNode json = objectMapper.readTree(responseBody);
                String predicted = json.path("predictedClass").asText();
                double confidence = json.path("confidence").asDouble(-1);
                String modelVersion = json.path("modelVersion").asText("");
                if (predicted.isBlank() || confidence < 0 || modelVersion.isBlank()) throw new IllegalStateException("AI service returned an invalid prediction");
                return new InferenceResult(PredictionClass.valueOf(predicted), confidence, modelVersion, false);
            } finally {
                connection.disconnect();
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Could not reach the AI service", exception);
        }
    }

    private byte[] multipartBody(Path imagePath, String boundary) throws IOException {
        String contentType = imagePath.toString().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(output, "--" + boundary + "\r\n");
        write(output, "Content-Disposition: form-data; name=\"image\"; filename=\"" + imagePath.getFileName() + "\"\r\n");
        write(output, "Content-Type: " + contentType + "\r\n\r\n");
        output.write(Files.readAllBytes(imagePath));
        write(output, "\r\n--" + boundary + "--\r\n");
        return output.toByteArray();
    }

    private void write(ByteArrayOutputStream output, String value) throws IOException { output.write(value.getBytes(StandardCharsets.UTF_8)); }
}
