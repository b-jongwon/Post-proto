package com.facthub.user.service;

import com.facthub.common.exception.BusinessException;
import com.facthub.user.domain.EmailVerification;
import com.facthub.user.dto.EmailVerificationConfirmRequest;
import com.facthub.user.dto.EmailVerificationConfirmResponse;
import com.facthub.user.dto.EmailVerificationIssueResponse;
import com.facthub.user.dto.EmailVerificationRequest;
import com.facthub.user.repository.EmailVerificationRepository;
import com.facthub.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EmailVerificationService {

    private final EmailVerificationRepository
            verificationRepository;
    private final UserRepository userRepository;
    private final SecureTokenHasher tokenHasher;
    private final VerificationEmailSender emailSender;
    private final SecureRandom secureRandom =
            new SecureRandom();

    private final int codeTtlMinutes;
    private final int tokenTtlMinutes;
    private final int resendCooldownSeconds;
    private final int maxAttempts;

    public EmailVerificationService(
            EmailVerificationRepository
                    verificationRepository,
            UserRepository userRepository,
            SecureTokenHasher tokenHasher,
            VerificationEmailSender emailSender,
            @Value("${facthub.email-verification.code-ttl-minutes}")
            int codeTtlMinutes,
            @Value("${facthub.email-verification.token-ttl-minutes}")
            int tokenTtlMinutes,
            @Value("${facthub.email-verification.resend-cooldown-seconds}")
            int resendCooldownSeconds,
            @Value("${facthub.email-verification.max-attempts}")
            int maxAttempts
    ) {
        this.verificationRepository =
                verificationRepository;
        this.userRepository = userRepository;
        this.tokenHasher = tokenHasher;
        this.emailSender = emailSender;
        this.codeTtlMinutes = codeTtlMinutes;
        this.tokenTtlMinutes = tokenTtlMinutes;
        this.resendCooldownSeconds =
                resendCooldownSeconds;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public EmailVerificationIssueResponse issue(
            EmailVerificationRequest request
    ) {
        String email = normalizeEmail(request.email());
        LocalDateTime now = LocalDateTime.now();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(
                    "EMAIL_ALREADY_EXISTS",
                    "이미 사용 중인 이메일입니다.",
                    HttpStatus.CONFLICT
            );
        }

        verificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .ifPresent(latest ->
                        validateResendCooldown(
                                latest,
                                now
                        )
                );

        String code = "%06d".formatted(
                secureRandom.nextInt(1_000_000)
        );

        LocalDateTime expiresAt =
                now.plusMinutes(codeTtlMinutes);

        EmailVerification verification =
                EmailVerification.issue(
                        email,
                        tokenHasher.hash(code),
                        expiresAt
                );

        verificationRepository.save(verification);
        emailSender.send(
                email,
                code,
                codeTtlMinutes
        );

        return new EmailVerificationIssueResponse(
                email,
                expiresAt,
                resendCooldownSeconds,
                emailSender.isLocalPreviewMode()
                        ? code
                        : null
        );
    }

    @Transactional(
            noRollbackFor = BusinessException.class
    )
    public EmailVerificationConfirmResponse confirm(
            EmailVerificationConfirmRequest request
    ) {
        String email = normalizeEmail(request.email());
        LocalDateTime now = LocalDateTime.now();

        EmailVerification verification =
                verificationRepository
                        .findTopByEmailOrderByCreatedAtDesc(
                                email
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "VERIFICATION_NOT_REQUESTED",
                                        "먼저 이메일 인증 코드를 요청해주세요.",
                                        HttpStatus.BAD_REQUEST
                                )
                        );

        if (verification.isExpired(now)) {
            throw new BusinessException(
                    "VERIFICATION_CODE_EXPIRED",
                    "인증 코드가 만료되었습니다. 새 코드를 요청해주세요.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (verification.isLocked(maxAttempts)) {
            throw new BusinessException(
                    "VERIFICATION_ATTEMPTS_EXCEEDED",
                    "인증 시도 횟수를 초과했습니다. 새 코드를 요청해주세요.",
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }

        if (!verification.codeMatches(
                tokenHasher.hash(request.code())
        )) {
            verification.recordFailedAttempt();

            throw new BusinessException(
                    "INVALID_VERIFICATION_CODE",
                    "인증 코드가 올바르지 않습니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        String plainToken =
                UUID.randomUUID()
                        + "."
                        + UUID.randomUUID();

        verification.markVerified(
                tokenHasher.hash(plainToken),
                now
        );

        return new EmailVerificationConfirmResponse(
                plainToken,
                now.plusMinutes(tokenTtlMinutes)
        );
    }

    @Transactional
    public void consumeVerifiedEmail(
            String email,
            String plainToken
    ) {
        LocalDateTime now = LocalDateTime.now();

        EmailVerification verification =
                verificationRepository
                        .findByEmailAndTokenHash(
                                normalizeEmail(email),
                                tokenHasher.hash(
                                        plainToken.trim()
                                )
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "EMAIL_VERIFICATION_REQUIRED",
                                        "이메일 인증을 완료해주세요.",
                                        HttpStatus.BAD_REQUEST
                                )
                        );

        LocalDateTime tokenExpiresAt =
                verification
                        .getVerifiedAt()
                        .plusMinutes(tokenTtlMinutes);

        if (!verification.canBeConsumed(
                now,
                tokenExpiresAt
        )) {
            throw new BusinessException(
                    "EMAIL_VERIFICATION_EXPIRED",
                    "이메일 인증이 만료되었습니다. 다시 인증해주세요.",
                    HttpStatus.BAD_REQUEST
            );
        }

        verification.consume(now);
    }

    private void validateResendCooldown(
            EmailVerification latest,
            LocalDateTime now
    ) {
        long elapsedSeconds =
                Duration.between(
                        latest.getCreatedAt(),
                        now
                ).getSeconds();

        if (elapsedSeconds
                < resendCooldownSeconds) {

            long remaining =
                    resendCooldownSeconds
                            - elapsedSeconds;

            throw new BusinessException(
                    "VERIFICATION_RESEND_TOO_SOON",
                    "%d초 후에 인증 코드를 다시 요청할 수 있습니다."
                            .formatted(remaining),
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }
    }

    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}

