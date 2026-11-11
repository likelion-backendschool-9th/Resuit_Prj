package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

import com.newlecture.resuit.entity.PaidFeedbackRequest;

public record PaidFeedbackSummary(
		Long id,
		UserSummary requester,
		UserSummary mentor,
		String resumeTitle,
		int credits,
		String status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	public static PaidFeedbackSummary from(PaidFeedbackRequest request) {
		return new PaidFeedbackSummary(
				request.getId(),
				UserSummary.from(request.getRequester()),
				UserSummary.from(request.getMentor().getUser()),
				request.getCommonResume() != null
						? request.getCommonResume().getTitle()
						: request.getTargetResume().getTitle(),
				request.getCredits(),
				request.getStatus(),
				request.getCreatedAt(),
				request.getUpdatedAt());
	}
}
