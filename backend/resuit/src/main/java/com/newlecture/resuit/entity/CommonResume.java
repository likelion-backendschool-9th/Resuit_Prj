package com.newlecture.resuit.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 공통 이력서 (common_resumes). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResume {
    private Long id;
    private UUID userId;
    private String title;
    private Field field;
    /** jsonb에 저장할 JSON 문자열. DB 연동 시 JSON 변환이 필요하다. */
    private String content;
    private LocalDateTime createdAt;
    /** ERD의 update_at 컬럼. */
    private LocalDateTime updateAt;

    /** DB enum 값 frontend, backend에 대응한다. */
    public enum Field {
        FRONTEND,
        BACKEND
    }
}
