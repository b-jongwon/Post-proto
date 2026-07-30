package com.facthub.user.service;

import com.facthub.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class VerificationEmailSender {

    private final JavaMailSender mailSender;
    private final Environment environment;
    private final String deliveryMode;
    private final String from;

    public VerificationEmailSender(
            JavaMailSender mailSender,
            Environment environment,
            @Value("${facthub.mail.delivery-mode}")
            String deliveryMode,
            @Value("${facthub.mail.from}")
            String from
    ) {
        this.mailSender = mailSender;
        this.environment = environment;
        this.deliveryMode = deliveryMode.trim();
        this.from = from.trim();
    }

    public void send(
            String email,
            String code,
            int ttlMinutes
    ) {
        if (isLocalPreviewMode()) {
            return;
        }

        if (!"smtp".equalsIgnoreCase(deliveryMode)) {
            throw new BusinessException(
                    "EMAIL_DELIVERY_NOT_CONFIGURED",
                    "운영 이메일 발송 설정이 필요합니다.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(email);
        message.setSubject(
                "[FactHub] 회원가입 이메일 인증 코드"
        );
        message.setText(
                """
                FactHub 회원가입 인증 코드는 %s 입니다.

                인증 코드는 %d분 동안 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해주세요.
                """.formatted(code, ttlMinutes)
        );

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new BusinessException(
                    "EMAIL_DELIVERY_FAILED",
                    "인증 메일을 발송하지 못했습니다. 잠시 후 다시 시도해주세요.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    public boolean isLocalPreviewMode() {
        return "log".equalsIgnoreCase(deliveryMode)
                && environment.acceptsProfiles(
                        Profiles.of("local")
                );
    }
}

