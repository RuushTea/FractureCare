package com.fracturecare.prediction;

import com.fracturecare.config.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "real", matchIfMissing = true)
public class HttpInferenceClient implements InferenceClient {
    private final RestClient client;

    public HttpInferenceClient(AppProperties properties, ObjectMapper objectMapper) {
        Duration timeout = properties.ai().timeout() == null ? Duration.ofSeconds(30) : properties.ai().timeout();
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(timeout);
        this.client = RestClient.builder().baseUrl(properties.ai().baseUrl().replaceAll("/+$", "") + "/").requestFactory(factory).build();
    }

    @Override
    public InferenceResult predict(Path imagePath) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        MediaType type = imagePath.toString().toLowerCase().endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        body.part("image", new FileSystemResource(imagePath)).contentType(type);
        JsonNode response = client.post().uri("predict").contentType(MediaType.MULTIPART_FORM_DATA).body(body.build()).retrieve().body(JsonNode.class);
        if (response == null) throw new IllegalStateException("AI service returned an empty response");
        String predicted = response.path("predictedClass").asText();
        double confidence = response.path("confidence").asDouble(-1);
        String modelVersion = response.path("modelVersion").asText("");
        if (predicted.isBlank() || confidence < 0 || modelVersion.isBlank()) throw new IllegalStateException("AI service returned an invalid prediction");
        try { return new InferenceResult(PredictionClass.valueOf(predicted), confidence, modelVersion, false); }
        catch (IllegalArgumentException e) { throw new IllegalStateException("AI service returned an unknown prediction class", e); }
    }
}
