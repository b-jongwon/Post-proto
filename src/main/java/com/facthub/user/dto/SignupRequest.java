package com.facthub.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(
                max = 255,
                message = "이메일은 255자 이하여야 합니다."
        )
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(
                min = 8,
                max = 64,
                message = "비밀번호는 8자 이상 64자 이하여야 합니다."
        )
        String password,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(
                min = 2,
                max = 20,
                message = "닉네임은 2자 이상 20자 이하여야 합니다."
        )
        String nickname,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(
                min = 2,
                max = 50,
                message = "이름은 2자 이상 50자 이하여야 합니다."
        )
        String fullName,

        @NotNull(message = "출생연도는 필수입니다.")
        @Min(
                value = 1900,
                message = "출생연도는 1900년 이후여야 합니다."
        )
        @Max(
                value = 2100,
                message = "출생연도를 확인해주세요."
        )
        Integer birthYear,

        @NotBlank(message = "이메일 인증이 필요합니다.")
        @Size(
                max = 200,
                message = "이메일 인증 정보가 올바르지 않습니다."
        )
        String emailVerificationToken
) {
}
