package com.facthub.user.repository;

import com.facthub.user.domain.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository
        extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification>
    findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<EmailVerification>
    findByEmailAndTokenHash(
            String email,
            String tokenHash
    );
}

