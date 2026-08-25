package com.fracturecare.security;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.fracturecare.config.AppProperties;
import com.fracturecare.user.UserAccount;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtTokenService {
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final ObjectMapper objectMapper;
    private final AppProperties properties;
    private final Clock clock;

    public JwtTokenService(ObjectMapper objectMapper, AppProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.clock = Clock.systemUTC();
        if (properties.security().jwtSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("APP_JWT_SECRET must contain at least 32 bytes");
        }
    }

    public String issue(UserAccount user) {
        Instant now = clock.instant();
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", user.getEmail());
        claims.put("uid", user.getId());
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", now.plus(properties.security().tokenTtl()).getEpochSecond());
        try {
            String encodedHeader = encode(HEADER.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = encode(objectMapper.writeValueAsBytes(claims));
            String unsigned = encodedHeader + "." + encodedPayload;
            return unsigned + "." + encode(sign(unsigned));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not issue authentication token", exception);
        }
    }

    public AuthenticatedUser verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Malformed token");
            }
            byte[] expected = sign(parts[0] + "." + parts[1]);
            byte[] supplied = URL_DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expected, supplied)) {
                throw new IllegalArgumentException("Invalid token signature");
            }
            Map<String, Object> claims = objectMapper.readValue(URL_DECODER.decode(parts[1]), new TypeReference<>() {});
            long expiresAt = ((Number) claims.get("exp")).longValue();
            if (clock.instant().getEpochSecond() >= expiresAt) {
                throw new IllegalArgumentException("Token has expired");
            }
            return new AuthenticatedUser(((Number) claims.get("uid")).longValue(), (String) claims.get("sub"));
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid token", exception);
        }
    }

    private byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(properties.security().jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    private String encode(byte[] value) {
        return URL_ENCODER.encodeToString(value);
    }
}
