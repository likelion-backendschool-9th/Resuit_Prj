package com.newlecture.resuit.dto;

import java.time.LocalDateTime;
import com.newlecture.resuit.entity.JobApplication;

public record JobApplicationCommonResponse(
        Long id, Long jobId, Long commonResumeId, String status, LocalDateTime appliedAt) {
    public static JobApplicationCommonResponse from(JobApplication application) {
        return new JobApplicationCommonResponse(application.getId(), application.getJob().getId(),
                application.getCommonResume().getId(), application.getStatus(), application.getAppliedAt());
    }
}
