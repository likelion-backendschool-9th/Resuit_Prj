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
@Table(name = "target_resumes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TargetResume extends BaseTimeEntity {

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
    @JoinColumn(name = "common_resume_id")
    private CommonResume commonResume;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> content;

    // 특화 이력서 사진 주소. 만들 때 공통 이력서 사진을 복사하고, 그 뒤로는 따로 바꾼다.
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    // 3.11 특화 이력서 수정. 제목과 내용(요구 문항 답변 포함)만 바꾼다.
    // 어느 공고용인지(job), 어떤 공통 이력서에서 시작했는지(commonResume)는 바꾸지 않는다.
    // 사진을 바꾸거나(주소) 지운다(null).
    public void changePhoto(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void update(String title, Map<String, Object> content) {
        this.title = Objects.requireNonNull(title);
        this.content = Objects.requireNonNull(content);
    }
}
