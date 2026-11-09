package com.newlecture.resuit.dto;

import java.time.LocalDateTime;
import com.newlecture.resuit.entity.JobApplication;

public record JobApplicationTargetResponse(
        Long id, Long jobId, Long targetResumeId, String status, LocalDateTime appliedAt) {
    public static JobApplicationTargetResponse from(JobApplication application) {
        return new JobApplicationTargetResponse(application.getId(), application.getJob().getId(),
                application.getTargetResume().getId(), application.getStatus(), application.getAppliedAt());
    }
}
