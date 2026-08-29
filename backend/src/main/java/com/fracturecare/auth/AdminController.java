package com.fracturecare.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/professionals")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.UserResponse createProfessional(@Valid @RequestBody AuthDtos.AdminCreateProfessionalRequest request) {
        return authService.createProfessional(request).user();
    }
}
