package com.newlecture.resuit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** job 테이블의 JPA 엔티티. */
@Entity
@Table(name = "job")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "type", length = 20)
    private String type;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "location", length = 50)
    private String location;

    @Column(name = "employment_type", length = 30)
    private String employmentType;

    @Column(name = "career", length = 30)
    private String career;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "requirements", columnDefinition = "text")
    private String requirements;

    @Column(name = "preferred", columnDefinition = "text")
    private String preferred;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "company_id")
    private UUID companyId;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
