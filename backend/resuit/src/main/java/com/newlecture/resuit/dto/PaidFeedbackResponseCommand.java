package com.newlecture.resuit.dto;

public record PaidFeedbackResponseCommand(
		Long requestId,
		String username,
		String content) {
}
