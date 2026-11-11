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
@Table(name = "feedback_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FeedbackRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "common_resume_id")
    private CommonResume commonResume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_resume_id")
    private TargetResume targetResume;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "category", length = 20)
    private String category;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 3.3 피드백 요청 취소. 행을 지우지 않고 취소 시각만 남긴다. 이미 취소했으면 그대로 둔다.
    public void cancel(LocalDateTime now) {
        if (deletedAt == null) {
            deletedAt = Objects.requireNonNull(now);
        }
    }

    public boolean isCanceled() {
        return deletedAt != null;
    }
}
