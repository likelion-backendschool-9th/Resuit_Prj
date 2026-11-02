package com.newlecture.resuit.entity;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 채용 공고의 요구 문항 (job_questions). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobQuestion {
    private UUID id;
    private String content;
    private Integer charLimit;
    private Integer orderNo;
    private UUID jobId;
}
