package com.facthub.user.service;

import com.facthub.common.exception.DuplicateUserException;
import com.facthub.common.exception.InvalidLoginException;
import com.facthub.user.domain.User;
import com.facthub.user.domain.UserStatus;
import com.facthub.user.dto.SignupRequest;
import com.facthub.user.dto.SignupResponse;
import com.facthub.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String nickname = request.nickname().trim();

        validateDuplicateEmail(email);
        validateDuplicateNickname(nickname);

        String encodedPassword =
                passwordEncoder.encode(request.password());

        User user = User.createUser(
                email,
                encodedPassword,
                nickname
        );

        try {
            User savedUser = userRepository.save(user);
            return SignupResponse.from(savedUser);

        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUserException(
                    "DUPLICATE_USER",
                    "이미 사용 중인 이메일 또는 닉네임입니다."
            );
        }
    }

    public User getActiveUserByEmail(String inputEmail) {

        String email = inputEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidLoginException::new);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidLoginException();
        }

        return user;
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException(
                    "EMAIL_ALREADY_EXISTS",
                    "이미 사용 중인 이메일입니다."
            );
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new DuplicateUserException(
                    "NICKNAME_ALREADY_EXISTS",
                    "이미 사용 중인 닉네임입니다."
            );
        }
    }
}