package com.fracturecare.auth;

import com.fracturecare.common.ConflictException;
import com.fracturecare.common.UnauthorizedException;
import com.fracturecare.config.AppProperties;
import com.fracturecare.security.JwtTokenService;
import com.fracturecare.user.UserAccount;
import com.fracturecare.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import com.fracturecare.user.AccountRole;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final LoginAttemptService loginAttempts;
    private final AppProperties properties;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtTokenService tokenService,
                       LoginAttemptService loginAttempts, AppProperties properties) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.loginAttempts = loginAttempts;
        this.properties = properties;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account already exists for this email address.");
        }
        UserAccount user = users.save(new UserAccount(
                request.fullName().trim(), email, blankToNull(request.address()),
                passwordEncoder.encode(request.password()), Instant.now()));
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = normalizeEmail(request.email());
        loginAttempts.checkAllowed(email);
        UserAccount user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttempts.failed(email);
            throw new UnauthorizedException("The email address or password is incorrect.");
        }
        loginAttempts.succeeded(email);
        return response(user);
    }

    @Transactional
    public AuthDtos.AuthResponse registerProfessional(AuthDtos.ProfessionalRegisterRequest request) {
        String email = normalizeEmail(request.email());
        String username = normalizeUsername(request.username());
        if (users.existsByEmailIgnoreCase(email)) throw new ConflictException("An account already exists for this email address.");
        if (users.existsByUsernameIgnoreCase(username)) throw new ConflictException("That username is already in use.");
        UserAccount user = users.save(new UserAccount(request.fullName().trim(), email, username,
                passwordEncoder.encode(request.password()), AccountRole.MEDICAL_PROFESSIONAL, Instant.now()));
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse loginProfessional(AuthDtos.ProfessionalLoginRequest request) {
        String username = normalizeUsername(request.username());
        loginAttempts.checkAllowed("professional:" + username);
        UserAccount user = users.findByUsernameIgnoreCase(username).orElse(null);
        if (user == null || user.getRole() != AccountRole.MEDICAL_PROFESSIONAL || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttempts.failed("professional:" + username);
            throw new UnauthorizedException("The username or password is incorrect.");
        }
        loginAttempts.succeeded("professional:" + username);
        return response(user);
    }

    private AuthDtos.AuthResponse response(UserAccount user) {
        return new AuthDtos.AuthResponse(tokenService.issue(user), "Bearer",
                properties.security().tokenTtl().toSeconds(), AuthDtos.UserResponse.from(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeUsername(String username) { return username.trim().toLowerCase(Locale.ROOT); }
}
