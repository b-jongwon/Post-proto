package com.facthub.user.service;

import com.facthub.common.exception.BusinessException;
import com.facthub.user.domain.EmailVerification;
import com.facthub.user.dto.EmailVerificationConfirmRequest;
import com.facthub.user.dto.EmailVerificationConfirmResponse;
import com.facthub.user.dto.EmailVerificationIssueResponse;
import com.facthub.user.dto.EmailVerificationRequest;
import com.facthub.user.repository.EmailVerificationRepository;
import com.facthub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationServiceTest {

    private EmailVerificationRepository
            verificationRepository;
    private UserRepository userRepository;
    private VerificationEmailSender emailSender;
    private SecureTokenHasher tokenHasher;
    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        verificationRepository = Mockito.mock(
                EmailVerificationRepository.class
        );
        userRepository = Mockito.mock(
                UserRepository.class
        );
        emailSender = Mockito.mock(
                VerificationEmailSender.class
        );
        tokenHasher = new SecureTokenHasher();
        service = new EmailVerificationService(
                verificationRepository,
                userRepository,
                tokenHasher,
                emailSender,
                10,
                30,
                60,
                5
        );
    }

    @Test
    void issue_normalizesEmailAndReturnsLocalCode() {
        when(userRepository.existsByEmail(
                "member@example.com"
        )).thenReturn(false);
        when(verificationRepository
                .findTopByEmailOrderByCreatedAtDesc(
                        "member@example.com"
                )).thenReturn(Optional.empty());
        when(emailSender.isLocalPreviewMode())
                .thenReturn(true);

        EmailVerificationIssueResponse response =
                service.issue(
                        new EmailVerificationRequest(
                                "  MEMBER@Example.COM "
                        )
                );

        assertThat(response.email())
                .isEqualTo("member@example.com");
        assertThat(response.developmentCode())
                .matches("\\d{6}");
        assertThat(response.retryAfterSeconds())
                .isEqualTo(60);
        assertThat(response.expiresAt())
                .isAfter(LocalDateTime.now());

        ArgumentCaptor<EmailVerification> captor =
                ArgumentCaptor.forClass(
                        EmailVerification.class
                );

        verify(verificationRepository)
                .save(captor.capture());
        assertThat(captor.getValue().getEmail())
                .isEqualTo("member@example.com");

        verify(emailSender).send(
                "member@example.com",
                response.developmentCode(),
                10
        );
    }

    @Test
    void issue_rejectsExistingAccount() {
        when(userRepository.existsByEmail(
                "member@example.com"
        )).thenReturn(true);

        assertThatThrownBy(() ->
                service.issue(
                        new EmailVerificationRequest(
                                "member@example.com"
                        )
                )
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo("EMAIL_ALREADY_EXISTS");
    }

    @Test
    void confirmAndConsume_acceptsOneVerifiedToken() {
        String email = "member@example.com";
        String code = "123456";

        EmailVerification verification =
                EmailVerification.issue(
                        email,
                        tokenHasher.hash(code),
                        LocalDateTime.now()
                                .plusMinutes(10)
                );

        when(verificationRepository
                .findTopByEmailOrderByCreatedAtDesc(
                        email
                )).thenReturn(
                        Optional.of(verification)
                );

        EmailVerificationConfirmResponse response =
                service.confirm(
                        new EmailVerificationConfirmRequest(
                                email,
                                code
                        )
                );

        assertThat(response.verificationToken())
                .isNotBlank();
        assertThat(response.expiresAt())
                .isAfter(LocalDateTime.now());

        when(verificationRepository
                .findByEmailAndTokenHash(
                        email,
                        tokenHasher.hash(
                                response.verificationToken()
                        )
                )).thenReturn(
                        Optional.of(verification)
                );

        service.consumeVerifiedEmail(
                email,
                response.verificationToken()
        );

        assertThat(
                verification.canBeConsumed(
                        LocalDateTime.now(),
                        LocalDateTime.now()
                                .plusMinutes(30)
                )
        ).isFalse();
    }

    @Test
    void confirm_recordsFailedAttemptForWrongCode() {
        String email = "member@example.com";
        EmailVerification verification =
                EmailVerification.issue(
                        email,
                        tokenHasher.hash("123456"),
                        LocalDateTime.now()
                                .plusMinutes(10)
                );

        when(verificationRepository
                .findTopByEmailOrderByCreatedAtDesc(
                        email
                )).thenReturn(
                        Optional.of(verification)
                );

        assertThatThrownBy(() ->
                service.confirm(
                        new EmailVerificationConfirmRequest(
                                email,
                                "654321"
                        )
                )
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo("INVALID_VERIFICATION_CODE");

        assertThat(verification.getFailedAttempts())
                .isEqualTo(1);
    }
}
