package com.newlecture.resuit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, unique = true, length = 50)
  private String handle;

  @Column(name = "avatar_color", length = 20)
  private String avatarColor;

  @Column(name = "github_username", length = 100)
  private String githubUsername;

  @Column(name = "is_company", nullable = false)
  private boolean company;

  public User(String name, String handle, String avatarColor, String githubUsername, boolean company) {
    this.name = name;
    this.handle = handle;
    this.avatarColor = avatarColor;
    this.githubUsername = githubUsername;
    this.company = company;
  }
}
