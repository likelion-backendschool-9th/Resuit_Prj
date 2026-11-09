package com.newlecture.resuit.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.*;
import java.util.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@org.hibernate.annotations.DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "job_applications", indexes = @Index(name = "idx_job_application_snapshot_expiry", columnList = "snapshot_expires_at"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "common_resume_id")
    private CommonResume commonResume;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "target_resume_id")
    private TargetResume targetResume;

    @Column(name = "submitted_resume_type", length = 20)
    private String submittedResumeType;

    @Column(name = "submitted_company_name", length = 100)
    private String submittedCompanyName;

    @Column(name = "submitted_resume_title", length = 100)
    private String submittedResumeTitle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "submitted_resume_snapshot", columnDefinition = "jsonb")
    private SubmittedResume submittedResumeSnapshot;

    @Column(name = "snapshot_photo_key", length = 500)
    private String snapshotPhotoKey;

    @Column(name = "snapshot_expires_at")
    private LocalDateTime snapshotExpiresAt;

    @Column(name = "snapshot_deleted_at")
    private LocalDateTime snapshotDeletedAt;

    @PrePersist
    private void initializeExpiry() {
        if (submittedResumeSnapshot != null) {
            if (appliedAt == null) appliedAt = LocalDateTime.now();
            snapshotExpiresAt = appliedAt.plusMonths(3);
        }
    }

    public boolean hasAvailableSnapshot(LocalDateTime now) {
        return submittedResumeSnapshot != null && snapshotDeletedAt == null
            && snapshotExpiresAt != null && now.isBefore(snapshotExpiresAt);
    }

    public void purgeSnapshot(LocalDateTime now) {
        submittedResumeSnapshot = null;
        snapshotPhotoKey = null;
        snapshotDeletedAt = now;
    }

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @CreatedDate
    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
    public void cancel() {
        if (canceledAt != null) {
            return;
        }
        status = "CANCELED";
        canceledAt = LocalDateTime.now();
    }
}
