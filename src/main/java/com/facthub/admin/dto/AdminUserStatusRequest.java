package com.facthub.admin.dto;

import com.facthub.user.domain.UserStatus;
import jakarta.validation.constraints.NotNull;

public record AdminUserStatusRequest(
        @NotNull(message = "회원 상태는 필수입니다.")
        UserStatus status
) {
}

