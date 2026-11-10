package com.newlecture.resuit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.*;
import java.util.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_post_id")
    private BlogPost blogPost;

    @Column(name = "repo_full_name", length = 200)
    private String repoFullName;

    // 태그가 없는 게시글은 컬렉션 테이블에 행을 만들지 않는다.
    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "tag"}),
            indexes = @Index(name = "idx_post_tags_tag_post", columnList = "tag, post_id"))
    @Column(name = "tag", nullable = false, length = 50)
    @org.hibernate.annotations.BatchSize(size = 100)
    @Builder.Default
    private Set<String> tags = new LinkedHashSet<>();

    public void replaceTags(Set<String> tags) {
        this.tags.clear();
        if (tags != null) this.tags.addAll(tags);
    }

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void update(
            String type,
            String content,
            String imageUrl,
            String videoUrl,
            String repoFullName) {
        this.type = type;
        this.content = content;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.repoFullName = repoFullName;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
