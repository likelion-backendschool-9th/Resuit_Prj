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
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uq_users_username", columnNames = "username"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "self_introduction", columnDefinition = "text")
    private String selfIntroduction;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "widgets", columnDefinition = "jsonb")
    private Map<String, Object> widgets;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pinned_common_resume_id")
    private CommonResume pinnedCommonResume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "displayed_badge_id")
    private Badge displayedBadge;

    @Column(name = "subscription")
    private Boolean subscription;

    // 값은 Region enum 이름(SEOUL 등)이다. 엔티티를 enum으로 두면 Hibernate가 값 목록으로 CHECK 제약을 만들어
    // 지역을 추가할 때 ddl-auto=update인 DB에서 저장이 막힌다. 그래서 문자열로 두고, 값 검증은 요청 DTO에서 Region으로 한다.
    @Column(name = "region", length = 20)
    private String region;

    public void updateProfile(String nickname, String selfIntroduction,
            String profileImageUrl, Map<String, Object> widgets) {
        if (nickname != null) this.nickname = nickname;
        if (selfIntroduction != null) this.selfIntroduction = selfIntroduction;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        if (widgets != null) this.widgets = widgets;
    }

    public void changeUsername(String username) {
        this.username = username;
    }

    public void pinResume(CommonResume resume) {
        this.pinnedCommonResume = resume;
    }

    public void displayBadge(Badge badge) {
        this.displayedBadge = badge;
    }

    // 프로필 편집에서 고른 지역(Region enum 이름)을 저장한다. 값 검증은 요청 DTO에서 Region으로 끝낸다.
    public void changeRegion(String region) {
        this.region = region;
    }

    public void changeRole(String role) {
        this.role = role;
    }
}
