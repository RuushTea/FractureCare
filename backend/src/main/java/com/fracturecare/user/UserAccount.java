package com.fracturecare.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.time.Instant;

@Entity
@Table(name = "users")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountRole role = AccountRole.USER;

    @Column(unique = true, length = 60)
    private String username;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected UserAccount() {}

    public UserAccount(String fullName, String email, String address, String passwordHash, Instant createdAt) {
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.role = AccountRole.USER;
    }

    public UserAccount(String fullName, String email, String username, String passwordHash, AccountRole role, Instant createdAt) {
        this(fullName, email, null, passwordHash, createdAt);
        this.username = username;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getCreatedAt() { return createdAt; }
    public AccountRole getRole() { return role; }
    public String getUsername() { return username; }
}
