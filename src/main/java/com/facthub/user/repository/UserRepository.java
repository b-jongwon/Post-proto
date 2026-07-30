package com.facthub.user.repository;

import com.facthub.user.domain.User;
import com.facthub.user.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    long countByStatus(UserStatus status);

    Page<User> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );
}
