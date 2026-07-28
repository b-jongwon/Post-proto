package com.facthub.user.dto;

import com.facthub.user.domain.User;

public record LoginResponse(
        Long userId,
        String email,
        String nickname,
        String role
) {

    public static LoginResponse from(User user) {
        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );
    }
}