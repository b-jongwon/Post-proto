package com.facthub.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "email",
            nullable = false,
            unique = true,
            length = 255
    )
    private String email;

    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    @Column(
            name = "nickname",
            nullable = false,
            unique = true,
            length = 50
    )
    private String nickname;

    @Column(
            name = "full_name",
            length = 50
    )
    private String fullName;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(
            name = "email_verified_at",
            nullable = false
    )
    private LocalDateTime emailVerifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 20
    )
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private UserStatus status;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    protected User() {
    }

    private User(
            String email,
            String passwordHash,
            String nickname,
            String fullName,
            Integer birthYear,
            LocalDateTime emailVerifiedAt,
            UserRole role,
            UserStatus status
    ) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.fullName = fullName;
        this.birthYear = birthYear;
        this.emailVerifiedAt = emailVerifiedAt;
        this.role = role;
        this.status = status;
    }

    public static User createUser(
            String email,
            String passwordHash,
            String nickname,
            String fullName,
            Integer birthYear
    ) {
        return new User(
                email,
                passwordHash,
                nickname,
                fullName,
                birthYear,
                LocalDateTime.now(),
                UserRole.USER,
                UserStatus.ACTIVE
        );
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public void promoteToAdmin() {
        this.role = UserRole.ADMIN;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public String getFullName() {
        return fullName;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public Integer getAge() {
        if (birthYear == null) {
            return null;
        }

        return Math.max(
                0,
                LocalDate.now().getYear() - birthYear
        );
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
