package com.facthub.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "email",
            nullable = false,
            length = 255
    )
    private String email;

    @Column(
            name = "code_hash",
            nullable = false,
            length = 64
    )
    private String codeHash;

    @Column(
            name = "token_hash",
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(
            name = "failed_attempts",
            nullable = false
    )
    private int failedAttempts;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    protected EmailVerification() {
    }

    private EmailVerification(
            String email,
            String codeHash,
            LocalDateTime expiresAt
    ) {
        this.email = email;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.failedAttempts = 0;
    }

    public static EmailVerification issue(
            String email,
            String codeHash,
            LocalDateTime expiresAt
    ) {
        return new EmailVerification(
                email,
                codeHash,
                expiresAt
        );
    }

    public boolean codeMatches(String inputHash) {
        return codeHash.equals(inputHash);
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isLocked(int maxAttempts) {
        return failedAttempts >= maxAttempts;
    }

    public void recordFailedAttempt() {
        this.failedAttempts++;
    }

    public void markVerified(
            String tokenHash,
            LocalDateTime verifiedAt
    ) {
        this.tokenHash = tokenHash;
        this.verifiedAt = verifiedAt;
    }

    public boolean canBeConsumed(
            LocalDateTime now,
            LocalDateTime tokenExpiresAt
    ) {
        return verifiedAt != null
                && consumedAt == null
                && now.isBefore(tokenExpiresAt);
    }

    public void consume(LocalDateTime now) {
        this.consumedAt = now;
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

