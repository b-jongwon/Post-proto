package com.facthub.user.dto;

import java.time.LocalDateTime;

public record EmailVerificationConfirmResponse(
        String verificationToken,
        LocalDateTime expiresAt
) {
}

