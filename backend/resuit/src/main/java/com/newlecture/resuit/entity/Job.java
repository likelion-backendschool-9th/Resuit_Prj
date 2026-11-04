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
@Table(name = "job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Job extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private User company;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "title", nullable = false, length = 100)
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

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
