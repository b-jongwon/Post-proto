package com.facthub.user.dto;

import java.time.LocalDateTime;

public record EmailVerificationIssueResponse(
        String email,
        LocalDateTime expiresAt,
        int retryAfterSeconds,
        String developmentCode
) {
}

