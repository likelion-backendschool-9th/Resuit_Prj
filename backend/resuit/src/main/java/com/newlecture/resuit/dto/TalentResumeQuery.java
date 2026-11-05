package com.newlecture.resuit.dto;

import java.util.UUID;

public record TalentResumeQuery(
		UUID userId,
		Long resumeId,
		String username) {
}
