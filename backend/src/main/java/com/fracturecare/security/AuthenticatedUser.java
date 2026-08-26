package com.fracturecare.security;

import com.fracturecare.user.AccountRole;

public record AuthenticatedUser(Long id, String email, String username, AccountRole role) {}
