package com.newlecture.resuit.dto;

import com.newlecture.resuit.entity.JobApplication;
import java.time.LocalDateTime;

public record MyJobApplicationResponse(
        boolean applied, Long jobId, Long id, String resumeType, Long resumeId,
        String status, LocalDateTime appliedAt) {
    public static MyJobApplicationResponse notApplied(Long jobId) {
        return new MyJobApplicationResponse(false, jobId, null, null, null, null, null);
    }

    public static MyJobApplicationResponse from(JobApplication application) {
        boolean common = "COMMON".equals(application.getSubmittedResumeType()) || application.getCommonResume() != null;
        Long resumeId = common
                ? (application.getCommonResume() == null ? null : application.getCommonResume().getId())
                : (application.getTargetResume() == null ? null : application.getTargetResume().getId());
        return new MyJobApplicationResponse(true, application.getJob().getId(), application.getId(),
                common ? "COMMON" : "TARGET", resumeId, application.getStatus(), application.getAppliedAt());
    }
}
