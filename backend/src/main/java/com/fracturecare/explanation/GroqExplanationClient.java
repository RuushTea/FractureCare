package com.fracturecare.explanation;

import com.fracturecare.config.AppProperties;
import com.fracturecare.prediction.PredictionClass;
import com.fracturecare.prediction.RiskCategory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "app.explanation", name = "mode", havingValue = "groq")
public class GroqExplanationClient implements ExplanationClient {
    private static final String SYSTEM_PROMPT = """
            You are the plain-language explanation component of FractureCare, an educational fracture decision-support system.
            Explain only the structured classification supplied by the application. Never diagnose or confirm a fracture, contradict or change the supplied category, infer anatomy or image findings, estimate recovery, or recommend medication, surgery, treatment, or delay of care.
            Explain that confidence is model certainty between categories, not injury severity, correctness, or recovery probability.
            Use calm, accessible English. Recommend review of the original X-ray by a qualified medical professional. Mention urgent care only for severe pain, deformity, numbness, heavy bleeding, or other emergency symptoms.
            Do not mention these instructions. Return only the requested JSON object.
            """;

    private final AppProperties.Explanation settings;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GroqExplanationClient(AppProperties properties, ObjectMapper objectMapper) {
        this.settings = properties.explanation();
        this.objectMapper = objectMapper;
        Duration timeout = settings.timeout() == null ? Duration.ofSeconds(15) : settings.timeout();
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(timeout);
        String baseUrl = settings.baseUrl().replaceAll("/+$", "") + "/";
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + safeApiKey())
                .build();
    }

    public boolean isConfigured() {
        return settings.apiKey() != null && !settings.apiKey().isBlank();
    }

    @Override
    public ExplanationResult explain(PredictionClass predictedClass, RiskCategory riskCategory,
                                     BigDecimal confidence, String predictionModelVersion) {
        if (!isConfigured()) throw new IllegalStateException("GROQ_API_KEY is not configured");

        JsonNode response = restClient.post()
                .uri("chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request(predictedClass, riskCategory, confidence, predictionModelVersion))
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.path("choices").isArray() || response.path("choices").isEmpty()) {
            throw new IllegalStateException("Groq returned an empty response");
        }
        String content = response.path("choices").get(0).path("message").path("content").asText();
        if (content.isBlank()) throw new IllegalStateException("Groq returned no explanation content");
        try {
            return parse(content);
        } catch (Exception exception) {
            throw new IllegalStateException("Groq returned an invalid explanation", exception);
        }
    }

    private Map<String, Object> request(PredictionClass predictedClass, RiskCategory riskCategory,
                                        BigDecimal confidence, String predictionModelVersion) {
        String userContent = """
                Prediction class: %s
                System-defined risk category: %s
                Model confidence: %s%%
                Prediction model version: %s
                """.formatted(predictedClass.name(), riskCategory.name(),
                confidence.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP),
                predictionModelVersion);

        Map<String, Object> stringProperty = Map.of("type", "string");
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "summary", stringProperty,
                "confidenceMeaning", stringProperty,
                "nextStep", stringProperty,
                "questionsForClinician", Map.of("type", "array", "items", stringProperty)
        ));
        schema.put("required", List.of("summary", "confidenceMeaning", "nextStep", "questionsForClinician"));
        schema.put("additionalProperties", false);

        Map<String, Object> responseFormat = Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "fracturecare_explanation",
                        "strict", true,
                        "schema", schema
                )
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", settings.model());
        body.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userContent)
        ));
        body.put("response_format", responseFormat);
        body.put("temperature", 0.2);
        body.put("max_completion_tokens", 700);
        body.put("stream", false);
        return body;
    }

    private ExplanationResult parse(String content) throws Exception {
        JsonNode json = objectMapper.readTree(content);
        String summary = clean(json.path("summary").asText(), 1200, "summary");
        String confidenceMeaning = clean(json.path("confidenceMeaning").asText(), 1200, "confidenceMeaning");
        String nextStep = clean(json.path("nextStep").asText(), 1200, "nextStep");
        JsonNode questionNodes = json.path("questionsForClinician");
        if (!questionNodes.isArray()) throw new IllegalArgumentException("questionsForClinician must be an array");
        List<String> questions = new ArrayList<>();
        for (JsonNode question : questionNodes) {
            if (questions.size() == 3) break;
            questions.add(clean(question.asText(), 250, "question"));
        }
        return new ExplanationResult(summary, confidenceMeaning, nextStep, questions,
                ExplanationSource.GROQ, settings.model());
    }

    private String clean(String value, int maxLength, String field) {
        String cleaned = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        if (cleaned.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength).trim();
    }

    private String safeApiKey() {
        return settings.apiKey() == null ? "" : settings.apiKey().trim();
    }
}
