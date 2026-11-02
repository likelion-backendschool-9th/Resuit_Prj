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
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = { "follower_id", "following_id" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "follower_id", nullable = false)
  private User follower;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "following_id", nullable = false)
  private User following;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public Follow(User follower, User following) {
    if (follower == following || (follower.getId() != null && follower.getId().equals(following.getId()))) {
      throw new IllegalArgumentException("A user cannot follow themselves");
    }
    this.follower = follower;
    this.following = following;
  }

  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
  }
}
