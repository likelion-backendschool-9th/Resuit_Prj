package com.newlecture.resuit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_badge_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadgeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "active_from", nullable = false)
    private LocalDateTime activeFrom;

    @Column(name = "active_until", nullable = false)
    private LocalDateTime activeUntil;

    public UserBadgeHistory(UserBadge userBadge, LocalDateTime activeUntil) {
        this.user = userBadge.getUser();
        this.badge = userBadge.getBadge();
        this.activeFrom = userBadge.getContinuousSince() != null
                ? userBadge.getContinuousSince() : userBadge.getAcquiredAt();
        this.activeUntil = activeUntil;
    }

    /** 로컬 데모 시드용. */
    public UserBadgeHistory(User user, Badge badge, LocalDateTime activeFrom,
            LocalDateTime activeUntil) {
        this.user = user;
        this.badge = badge;
        this.activeFrom = activeFrom;
        this.activeUntil = activeUntil;
    }
}
