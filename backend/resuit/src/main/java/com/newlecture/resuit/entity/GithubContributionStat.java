package com.newlecture.resuit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 인재 찾기에서 기여 수로 거를 때 GitHub API를 매번 부르지 않고 이 값만 읽는다. 값은 스케줄러가 주기적으로 갱신한다.
@Entity
@Table(name = "github_contribution_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubContributionStat {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total_contributions", nullable = false)
    private int totalContributions;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    public GithubContributionStat(User user, int totalContributions, LocalDateTime syncedAt) {
        this.user = user;
        this.totalContributions = totalContributions;
        this.syncedAt = syncedAt;
    }

    public void update(int totalContributions, LocalDateTime syncedAt) {
        this.totalContributions = totalContributions;
        this.syncedAt = syncedAt;
    }
}
