package com.fracturecare;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FractureCareFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void userCanRegisterUploadReviewHistoryAndDownloadAReport() throws Exception {
        String email = "test.user+" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String registration = "{\"fullName\":\"Test User\",\"email\":\"" + email + "\",\"address\":\"Colombo\",\"password\":\"SecurePass123\"}";
        String authJson = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value(email))
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(authJson).get("token").asText();

        MockMultipartFile image = new MockMultipartFile("image", "wrist-xray.png", "image/png", testPng());
        String predictionJson = mvc.perform(multipart("/api/predictions").file(image).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.simulated").value(true))
                .andExpect(jsonPath("$.explanation").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long predictionId = objectMapper.readTree(predictionJson).get("id").asLong();

        mvc.perform(post("/api/predictions/{id}/explanation", predictionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.explanation.source").value("RULES"))
                .andExpect(jsonPath("$.explanation.summary").isNotEmpty());

        mvc.perform(get("/api/predictions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(predictionId));

        String reportJson = mvc.perform(post("/api/predictions/{id}/report", predictionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode report = objectMapper.readTree(reportJson);

        mvc.perform(get("/api/reports/{id}/download", report.get("id").asLong())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("fracturecare-report")));
    }

    @Test
    void protectedEndpointsRejectAnonymousRequests() throws Exception {
        mvc.perform(get("/api/predictions")).andExpect(status().isUnauthorized());
    }

    private byte[] testPng() throws Exception {
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_GRAY);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
