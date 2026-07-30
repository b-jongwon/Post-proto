package com.facthub.admin.dto;

import jakarta.validation.constraints.NotNull;

public record AdminPostVisibilityRequest(
        @NotNull(message = "숨김 여부는 필수입니다.")
        Boolean hidden
) {
}

