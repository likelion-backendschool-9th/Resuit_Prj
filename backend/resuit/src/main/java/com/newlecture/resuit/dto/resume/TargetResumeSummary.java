package com.newlecture.resuit.dto.resume;

import com.newlecture.resuit.entity.TargetResume;
import java.time.LocalDateTime;

// 이력서 탭의 특화 이력서 줄. status: WRITING(작성 중) | SUBMITTED(지원 완료)
public record TargetResumeSummary(
    Long id,
    Long jobId,
    String jobTitle,
    String companyName,
    Long commonResumeId,
    long questionCount,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static TargetResumeSummary of(TargetResume resume, long questionCount, boolean submitted) {
        var job = resume.getJob();
        var base = resume.getCommonResume();
        return new TargetResumeSummary(resume.getId(), job.getId(), job.getTitle(), JobInfo.companyName(job),
            base == null ? null : base.getId(), questionCount, submitted ? "SUBMITTED" : "WRITING",
            resume.getCreatedAt(), resume.getUpdatedAt());
    }
}
