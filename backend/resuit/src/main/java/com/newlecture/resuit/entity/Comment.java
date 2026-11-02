package com.newlecture.resuit.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id")
  private Comment parentComment;

  @Lob
  @Column(nullable = false)
  private String body;

  @Column(length = 100)
  private String section;

  @Column(nullable = false)
  private boolean accepted;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public Comment(Post post, User author, Comment parentComment, String body, String section) {
    if (parentComment != null && parentComment.getPost() != post) {
      throw new IllegalArgumentException("A reply must belong to the same post");
    }
    this.post = post;
    this.author = author;
    this.parentComment = parentComment;
    this.body = body;
    this.section = section;
  }

  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
  }
}
