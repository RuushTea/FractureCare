package com.fracturecare.security;

import org.springframework.security.core.Authentication;

public final class CurrentUser {
    private CurrentUser() {}

    public static AuthenticatedUser from(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Authenticated user is unavailable");
        }
        return user;
    }
}
