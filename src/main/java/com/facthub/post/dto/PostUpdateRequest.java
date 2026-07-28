package com.facthub.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(

        @NotBlank(message = "제목은 필수입니다.")
        @Size(
                max = 200,
                message = "제목은 200자 이하여야 합니다."
        )
        String title,

        @NotBlank(message = "본문은 필수입니다.")
        @Size(
                max = 20000,
                message = "본문은 20,000자 이하여야 합니다."
        )
        String content,

        @NotBlank(message = "카테고리는 필수입니다.")
        @Size(
                max = 50,
                message = "카테고리는 50자 이하여야 합니다."
        )
        String category
) {
}