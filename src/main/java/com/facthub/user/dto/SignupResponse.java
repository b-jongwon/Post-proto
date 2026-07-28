package com.facthub.user.dto;

import com.facthub.user.domain.User;

import java.time.LocalDateTime;

public record SignupResponse(
        Long userId,
        String email,
        String nickname,
        String role,
        String status,
        LocalDateTime createdAt
) {

    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }
}