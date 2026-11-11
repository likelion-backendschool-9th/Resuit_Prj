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
@Table(name = "mentors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Mentor {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    // 직무: BACKEND, FRONTEND, FULLSTACK. 직접 입력하던 때의 값이 남아 있을 수 있다.
    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    @Column(name = "price_credits", nullable = false)
    private int priceCredits;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    // 현직자가 직무·피드백 가격을 고친다. 경력은 프로필 경력(user_careers)으로 계산한다.
    public void updateProfile(String jobTitle, int priceCredits) {
        this.jobTitle = jobTitle;
        this.priceCredits = priceCredits;
    }

    // 다른 회사 이메일로 다시 인증하면 회사명과 인증 시각을 바꾼다.
    public void changeCompany(String companyName, LocalDateTime verifiedAt) {
        this.companyName = companyName;
        this.verifiedAt = verifiedAt;
    }
}
