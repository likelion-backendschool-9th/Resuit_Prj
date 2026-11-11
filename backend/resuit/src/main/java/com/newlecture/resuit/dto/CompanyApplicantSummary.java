package com.newlecture.resuit.dto;

import com.newlecture.resuit.entity.JobApplication;
import java.time.LocalDateTime;

public record CompanyApplicantSummary(Long applicationId, Applicant applicant, String resumeType,
        Long resumeId, String resumeTitle, String status, LocalDateTime appliedAt,
        LocalDateTime snapshotExpiresAt, boolean snapshotAvailable) {
    public record Applicant(String username, String nickname, String profileImageUrl) {}
    public static CompanyApplicantSummary from(JobApplication a, LocalDateTime now) {
        var user = a.getUser();
        Long sourceId = a.getCommonResume() != null ? a.getCommonResume().getId()
            : a.getTargetResume() != null ? a.getTargetResume().getId() : null;
        return new CompanyApplicantSummary(a.getId(), new Applicant(user.getUsername(), user.getNickname(),
            user.getProfileImageUrl()), a.getSubmittedResumeType(), sourceId, a.getSubmittedResumeTitle(),
            a.getStatus(), a.getAppliedAt(), a.getSnapshotExpiresAt(), a.hasAvailableSnapshot(now));
    }
}
