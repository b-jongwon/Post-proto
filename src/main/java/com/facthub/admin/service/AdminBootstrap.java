package com.facthub.admin.service;

import com.facthub.user.domain.User;
import com.facthub.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
public class AdminBootstrap {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AdminBootstrap.class
            );

    private final UserRepository userRepository;
    private final String bootstrapEmail;

    public AdminBootstrap(
            UserRepository userRepository,
            @Value("${facthub.admin.bootstrap-email:}")
            String bootstrapEmail
    ) {
        this.userRepository = userRepository;
        this.bootstrapEmail = bootstrapEmail;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void promoteConfiguredUser() {
        if (!StringUtils.hasText(bootstrapEmail)) {
            return;
        }

        String normalizedEmail =
                bootstrapEmail
                        .trim()
                        .toLowerCase(Locale.ROOT);

        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElse(null);

        if (user == null) {
            log.warn(
                    "ADMIN_BOOTSTRAP_EMAIL에 해당하는 회원이 없습니다."
            );
            return;
        }

        if (!user.isAdmin()) {
            user.promoteToAdmin();
            log.info(
                    "설정된 회원을 관리자 역할로 승격했습니다."
            );
        }
    }
}

