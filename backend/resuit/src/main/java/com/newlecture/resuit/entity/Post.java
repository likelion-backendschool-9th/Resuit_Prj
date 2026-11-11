package com.newlecture.resuit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

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

    // 기술 정보 탭 전용 태그. 없으면 행을 만들지 않는다.
    @ElementCollection
    @CollectionTable(name = "post_tech_tags", joinColumns = @JoinColumn(name = "post_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "tag"}),
            indexes = @Index(name = "idx_post_tech_tags_tag_post", columnList = "tag, post_id"))
    @Column(name = "tag", nullable = false, length = 50)
    @org.hibernate.annotations.BatchSize(size = 100)
    @Builder.Default
    private Set<String> techTags = new LinkedHashSet<>();

    // 모든 게시글 타입의 자유 해시태그.
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 100)
    @Builder.Default
    private Set<PostHashtag> hashtags = new LinkedHashSet<>();

    public void replaceTechTags(Set<String> tags) {
        this.techTags.clear();
        if (tags != null) this.techTags.addAll(tags);
    }

    public void replaceHashtags(Set<String> tags) {
        this.hashtags.clear();
        if (tags != null) {
            for (String tag : tags) {
                this.hashtags.add(new PostHashtag(this, tag));
            }
        }
    }

    public Set<String> hashtagValues() {
        Set<String> values = new LinkedHashSet<>();
        for (PostHashtag hashtag : hashtags) {
            values.add(hashtag.getTag());
        }
        return values;
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
