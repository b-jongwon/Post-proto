package com.facthub.admin.domain;

import com.facthub.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_actions")
public class AdminAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "admin_id",
            nullable = false
    )
    private User admin;

    @Column(
            name = "action_type",
            nullable = false,
            length = 40
    )
    private String actionType;

    @Column(
            name = "target_type",
            nullable = false,
            length = 30
    )
    private String targetType;

    @Column(
            name = "target_id",
            nullable = false
    )
    private Long targetId;

    @Column(
            name = "description",
            nullable = false,
            length = 500
    )
    private String description;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    protected AdminAction() {
    }

    private AdminAction(
            User admin,
            String actionType,
            String targetType,
            Long targetId,
            String description
    ) {
        this.admin = admin;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.description = description;
    }

    public static AdminAction create(
            User admin,
            String actionType,
            String targetType,
            Long targetId,
            String description
    ) {
        return new AdminAction(
                admin,
                actionType,
                targetType,
                targetId,
                description
        );
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

