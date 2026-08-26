package com.fracturecare;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import javax.imageio.ImageIO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProfessionalReviewIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void consentReviewCompletionCreatesNotification() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String userToken = token(mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"Review User\",\"email\":\"user" + suffix + "@example.com\",\"password\":\"SecurePass123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String prediction = mvc.perform(multipart("/api/predictions").file(new MockMultipartFile("image", "xray.png", "image/png", png())).header("Authorization", "Bearer " + userToken)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long predictionId = mapper.readTree(prediction).get("id").asLong();
        mvc.perform(post("/api/predictions/{id}/professional-review", predictionId).header("Authorization", "Bearer " + userToken)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING"));
        String professionalToken = token(mvc.perform(post("/api/auth/professional/register").contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"Dr Review\",\"email\":\"doctor" + suffix + "@example.com\",\"username\":\"doctor_" + suffix + "\",\"password\":\"SecurePass123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String review = mvc.perform(get("/api/professional/reviews").header("Authorization", "Bearer " + professionalToken)).andExpect(status().isOk()).andExpect(jsonPath("$[0].predictionId").value(predictionId)).andReturn().getResponse().getContentAsString();
        long reviewId = mapper.readTree(review).get(0).get("reviewId").asLong();
        mvc.perform(post("/api/professional/reviews/{id}/complete", reviewId).header("Authorization", "Bearer " + professionalToken).contentType(MediaType.APPLICATION_JSON).content("{\"agreesWithAi\":true,\"comment\":\"Reviewed the provided image and agree with the system result.\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(get("/api/notifications").header("Authorization", "Bearer " + userToken)).andExpect(status().isOk()).andExpect(jsonPath("$[0].type").value("PROFESSIONAL_REVIEW_COMPLETED"));
    }

    private String token(String json) throws Exception { return mapper.readTree(json).get("token").asText(); }
    private byte[] png() throws Exception { BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_GRAY); ByteArrayOutputStream output = new ByteArrayOutputStream(); ImageIO.write(image, "png", output); return output.toByteArray(); }
}
