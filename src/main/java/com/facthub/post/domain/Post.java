package com.facthub.post.domain;

import com.facthub.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "author_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_posts_author"
            )
    )
    private User author;

    @Column(
            name = "title",
            nullable = false,
            length = 200
    )
    private String title;

    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String content;

    @Column(
            name = "category",
            nullable = false,
            length = 50
    )
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private PostStatus status;

    @Column(
            name = "view_count",
            nullable = false
    )
    private Long viewCount;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    protected Post() {
    }

    private Post(
            User author,
            String title,
            String content,
            String category
    ) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.category = category;
        this.status = PostStatus.PUBLISHED;
        this.viewCount = 0L;
    }

    public static Post create(
            User author,
            String title,
            String content,
            String category
    ) {
        return new Post(
                author,
                title,
                content,
                category
        );
    }

    public void update(
            String title,
            String content,
            String category
    ) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void delete() {
        this.status = PostStatus.DELETED;
    }

    public void hide() {
        this.status = PostStatus.HIDDEN;
    }

    public void publish() {
        this.status = PostStatus.PUBLISHED;
    }

    public boolean isWrittenBy(Long userId) {
        return author.getId().equals(userId);
    }

    public Long getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public PostStatus getStatus() {
        return status;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
