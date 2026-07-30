package com.facthub.admin.repository;

import com.facthub.admin.domain.AdminAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionRepository
        extends JpaRepository<AdminAction, Long> {
}

