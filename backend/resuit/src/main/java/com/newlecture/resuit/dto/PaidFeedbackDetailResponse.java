package com.newlecture.resuit.dto;

import java.util.List;

import com.newlecture.resuit.entity.PaidFeedbackRequest;

public record PaidFeedbackDetailResponse(
		PaidFeedbackSummary summary,
		Long commonResumeId,
		Long targetResumeId,
		String content,
		List<PaidFeedbackResponseSummary> responses) {

	public static PaidFeedbackDetailResponse of(
			PaidFeedbackRequest request,
			List<PaidFeedbackResponseSummary> responses) {
		return new PaidFeedbackDetailResponse(
				PaidFeedbackSummary.from(request),
				request.getCommonResume() == null ? null : request.getCommonResume().getId(),
				request.getTargetResume() == null ? null : request.getTargetResume().getId(),
				request.getContent(),
				responses);
	}
}
