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
@Table(name = "integrations", uniqueConstraints = @UniqueConstraint(name = "uq_integrations_user_provider", columnNames = {"user_id", "provider"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Integration extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    @Column(name = "platform", length = 30)
    private String platform;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "rss_url", length = 500)
    private String rssUrl;

    @Column(name = "access_token", length = 500)
    private String accessToken;

    @Builder.Default
    @Column(name = "connected", nullable = false)
    private boolean connected = false;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    public void connectBlog(String platform, String blogId, String rssUrl) {
        this.platform = platform;
        this.externalId = blogId;
        this.rssUrl = rssUrl;
        this.connected = true;
    }

    public void connectGithub(String login, String encryptedToken) {
        this.externalId = login;
        this.accessToken = encryptedToken;
        this.connected = true;
    }

    public void markSynced(LocalDateTime at) {
        this.lastSyncedAt = at;
    }

    public void disconnect() {
        this.connected = false;
        this.accessToken = null;
    }
}
