package com.facthub.admin.dto;

import com.facthub.user.domain.User;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long userId,
        String email,
        String nickname,
        String fullName,
        Integer birthYear,
        Integer age,
        String role,
        String status,
        LocalDateTime emailVerifiedAt,
        LocalDateTime createdAt
) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getFullName(),
                user.getBirthYear(),
                user.getAge(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getEmailVerifiedAt(),
                user.getCreatedAt()
        );
    }
}

