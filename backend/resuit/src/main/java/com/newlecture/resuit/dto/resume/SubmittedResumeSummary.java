package com.newlecture.resuit.dto.resume;

import java.time.LocalDateTime;
import com.newlecture.resuit.entity.JobApplication;

public record SubmittedResumeSummary(Long applicationId, String resumeType, String title,
        String companyName, String restoreTitle, LocalDateTime appliedAt, LocalDateTime expiresAt) {
    public static String restoreTitle(JobApplication application) {
        String title = application.getAppliedAt().toLocalDate() + "_" + application.getSubmittedCompanyName()
            + "_" + application.getSubmittedResumeTitle();
        return title.codePointCount(0, title.length()) > 100 ? title.substring(0, title.offsetByCodePoints(0, 100)) : title;
    }
    public static SubmittedResumeSummary from(JobApplication application) {
        return new SubmittedResumeSummary(application.getId(), application.getSubmittedResumeType(),
            application.getSubmittedResumeTitle(), application.getSubmittedCompanyName(), restoreTitle(application),
            application.getAppliedAt(), application.getSnapshotExpiresAt());
    }
}
