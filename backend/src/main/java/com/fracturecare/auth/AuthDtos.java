package com.fracturecare.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 120) String fullName,
            @NotBlank @Email @Size(max = 190) String email,
            @Size(max = 255) String address,
            @NotBlank @Size(min = 10, max = 72)
            @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                    message = "must include an uppercase letter, lowercase letter and number")
            String password
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record UserResponse(Long id, String fullName, String email, String address, Instant createdAt) {
        public static UserResponse from(com.fracturecare.user.UserAccount user) {
            return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getAddress(), user.getCreatedAt());
        }
    }

    public record AuthResponse(String token, String tokenType, long expiresInSeconds, UserResponse user) {}
}
