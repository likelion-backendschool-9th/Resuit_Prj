package com.newlecture.resuit.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record PaidFeedbackCreateRequest(
		@NotNull UUID mentorId,
		Long commonResumeId,
		Long targetResumeId,
		String content) {
}
