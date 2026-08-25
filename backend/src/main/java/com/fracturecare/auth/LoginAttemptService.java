package com.fracturecare.auth;

import com.fracturecare.common.TooManyRequestsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String email) {
        String key = normalize(email);
        Attempt attempt = attempts.get(key);
        if (attempt == null) return;
        if (attempt.windowStarted.plus(WINDOW).isBefore(Instant.now())) {
            attempts.remove(key);
            return;
        }
        if (attempt.failures >= MAX_FAILURES) {
            throw new TooManyRequestsException("Too many failed sign-in attempts. Try again in 15 minutes.");
        }
    }

    public void failed(String email) {
        String key = normalize(email);
        attempts.compute(key, (ignored, current) -> {
            Instant now = Instant.now();
            if (current == null || current.windowStarted.plus(WINDOW).isBefore(now)) {
                return new Attempt(1, now);
            }
            return new Attempt(current.failures + 1, current.windowStarted);
        });
    }

    public void succeeded(String email) {
        attempts.remove(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private record Attempt(int failures, Instant windowStarted) {}
}
