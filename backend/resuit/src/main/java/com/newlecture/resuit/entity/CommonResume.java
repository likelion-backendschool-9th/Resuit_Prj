package com.newlecture.resuit.entity;

import jakarta.persistence.*;
import java.util.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "common_resumes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CommonResume extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    public void updatePhoto(String photoUrl) { this.photoUrl = photoUrl; }

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "field", length = 20)
    private String field;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> content;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    // 3.6 공통 이력서 수정. 제목·분야·내용만 바꾼다. 작성자(user)는 바꾸지 않는다.
    public void update(String title, String field, Map<String, Object> content) {
        this.title = Objects.requireNonNull(title);
        this.field = field;
        this.content = Objects.requireNonNull(content);
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
