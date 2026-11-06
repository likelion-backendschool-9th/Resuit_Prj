package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

import com.newlecture.resuit.entity.JobApplication;

public record JobApplicationResponse(
		Long id,
		Long jobId,
		Long commonResumeId,
		String status,
		LocalDateTime appliedAt) {

	public static JobApplicationResponse from(JobApplication application) {
		return new JobApplicationResponse(
				application.getId(),
				application.getJob().getId(),
				application.getCommonResume() == null ? null : application.getCommonResume().getId(),
				application.getStatus(),
				application.getAppliedAt());
	}
}
