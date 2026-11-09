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
@Table(name = "company_subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CompanySubscription {

    @Id
    @Column(name = "company_id")
    private UUID companyId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private User company;

    @Column(name = "plan", nullable = false, length = 20)
    private String plan;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Pro 결제가 끝나면 체험을 끝내고 결제 기간으로 바꾼다. 만료 시각은 서비스에서 계산해 넘긴다.
    public void activatePaidPro(LocalDateTime expiresAt) {
        this.plan = "PRO";
        this.trialEndsAt = null;
        this.expiresAt = expiresAt;
    }
}
