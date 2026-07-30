package com.facthub.user.controller;

import com.facthub.common.response.ApiResponse;
import com.facthub.user.dto.EmailVerificationConfirmRequest;
import com.facthub.user.dto.EmailVerificationConfirmResponse;
import com.facthub.user.dto.EmailVerificationIssueResponse;
import com.facthub.user.dto.EmailVerificationRequest;
import com.facthub.user.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email-verifications")
public class EmailVerificationController {

    private final EmailVerificationService
            emailVerificationService;

    public EmailVerificationController(
            EmailVerificationService
                    emailVerificationService
    ) {
        this.emailVerificationService =
                emailVerificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EmailVerificationIssueResponse>
    issue(
            @Valid @RequestBody
            EmailVerificationRequest request
    ) {
        return ApiResponse.success(
                emailVerificationService.issue(
                        request
                )
        );
    }

    @PostMapping("/confirm")
    public ApiResponse<
            EmailVerificationConfirmResponse>
    confirm(
            @Valid @RequestBody
            EmailVerificationConfirmRequest request
    ) {
        return ApiResponse.success(
                emailVerificationService.confirm(
                        request
                )
        );
    }
}

