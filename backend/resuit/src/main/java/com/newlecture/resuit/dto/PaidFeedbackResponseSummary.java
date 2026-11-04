package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

import com.newlecture.resuit.entity.PaidFeedbackResponse;

public record PaidFeedbackResponseSummary(
		Long id,
		String content,
		LocalDateTime createdAt) {

	public static PaidFeedbackResponseSummary from(PaidFeedbackResponse response) {
		return new PaidFeedbackResponseSummary(
				response.getId(),
				response.getContent(),
				response.getCreatedAt());
	}
}
