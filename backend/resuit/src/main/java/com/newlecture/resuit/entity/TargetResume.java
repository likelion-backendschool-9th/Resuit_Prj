package com.newlecture.resuit.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 공고별 특화 이력서 (target_resumes). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetResume {
    private Long id;
    private UUID jobId;
    private UUID userId;
    private Long commonResumeId;
    private String title;
    /** jsonb에 저장할 JSON 문자열. DB 연동 시 JSON 변환이 필요하다. */
    private String content;
    private LocalDateTime createdAt;
    /** ERD의 update_at 컬럼. */
    private LocalDateTime updateAt;
}
