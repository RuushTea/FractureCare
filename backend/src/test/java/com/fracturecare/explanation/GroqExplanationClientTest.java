package com.fracturecare.explanation;

import com.fracturecare.config.AppProperties;
import com.fracturecare.prediction.PredictionClass;
import com.fracturecare.prediction.RiskCategory;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroqExplanationClientTest {
    @Test
    void sendsOnlyStructuredPredictionDataAndParsesTheExplanation() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/openai/v1/chat/completions", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] response = """
                    {"model":"openai/gpt-oss-20b","choices":[{"message":{"role":"assistant","content":"{\\"summary\\":\\"The model identified one possible fracture pattern.\\",\\"confidenceMeaning\\":\\"Confidence compares the model categories and is not injury severity.\\",\\"nextStep\\":\\"Ask a qualified clinician to review the original X-ray.\\",\\"questionsForClinician\\":[\\"Is follow-up imaging needed?\\"]}"}}]}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try {
            AppProperties properties = new AppProperties(null, null, null,
                    new AppProperties.Explanation("groq",
                            "http://127.0.0.1:" + server.getAddress().getPort() + "/openai/v1",
                            "${TEST_API_KEY}", "openai/gpt-oss-20b", Duration.ofSeconds(2)), null);
            GroqExplanationClient client = new GroqExplanationClient(properties, new ObjectMapper());

            ExplanationResult result = client.explain(PredictionClass.ONE_FRACTURE,
                    RiskCategory.LOW_RISK, new BigDecimal("0.8700"), "fracture-model-1");

            assertEquals(ExplanationSource.GROQ, result.source());
            assertEquals("openai/gpt-oss-20b", result.model());
            assertEquals("Bearer ${TEST_API_KEY}", authorization.get());
            assertTrue(requestBody.get().contains("ONE_FRACTURE"));
            assertTrue(requestBody.get().contains("87.0%"));
            assertFalse(requestBody.get().contains("email"));
            assertFalse(requestBody.get().contains("imageReference"));
        } finally {
            server.stop(0);
        }
    }
}
