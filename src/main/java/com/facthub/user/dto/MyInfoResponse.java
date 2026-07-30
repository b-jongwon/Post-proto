package com.facthub.user.dto;

import com.facthub.user.domain.User;

import java.time.LocalDateTime;

public record MyInfoResponse(
        Long userId,
        String email,
        String nickname,
        String fullName,
        Integer birthYear,
        Integer age,
        boolean emailVerified,
        LocalDateTime emailVerifiedAt,
        String role,
        String status,
        LocalDateTime createdAt
) {

    public static MyInfoResponse from(User user) {
        return new MyInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getFullName(),
                user.getBirthYear(),
                user.getAge(),
                user.isEmailVerified(),
                user.getEmailVerifiedAt(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }
}
