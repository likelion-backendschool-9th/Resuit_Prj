package com.newlecture.resuit.entity;

import java.time.Instant;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_badge", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "badge_id" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadge {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "badge_id", nullable = false)
  private Badge badge;

  @Column(name = "awarded_at", nullable = false, updatable = false)
  private Instant awardedAt;

  public UserBadge(User user, Badge badge) {
    this.user = user;
    this.badge = badge;
  }

  @PrePersist
  void onCreate() {
    awardedAt = Instant.now();
  }
}
