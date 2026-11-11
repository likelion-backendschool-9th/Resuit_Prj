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
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_badge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadge {

    @EmbeddedId
    private UserBadgeId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("badgeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @CreatedDate
    @Column(name = "acquired_at", nullable = false, updatable = false)
    private LocalDateTime acquiredAt;

    @Column(name = "is_eligible", nullable = false)
    private boolean isEligible;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BadgeStatus status;

    @Column(name = "grace_until")
    private LocalDateTime graceUntil;

    @Column(name = "continuous_since")
    private LocalDateTime continuousSince;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public UserBadge(User user, Badge badge) {
        this.id = new UserBadgeId(user.getId(), badge.getId());
        this.user = user;
        this.badge = badge;
        this.isEligible = true;
        this.status = BadgeStatus.ACTIVE;
    }

    public void activate(LocalDateTime at) {
        this.status = BadgeStatus.ACTIVE;
        this.isEligible = true;
        this.graceUntil = null;
        this.expiresAt = null;
        if (this.continuousSince == null) {
            this.continuousSince = at;
        }
    }

    public void requireRenewal(LocalDateTime nextEvaluationAt) {
        this.status = BadgeStatus.GRACE;
        this.isEligible = false;
        this.graceUntil = nextEvaluationAt;
        this.continuousSince = null;
    }

    public void expire(LocalDateTime at) {
        this.status = BadgeStatus.EXPIRED;
        this.isEligible = false;
        this.graceUntil = null;
        this.expiresAt = at;
        this.continuousSince = null;
    }
}
