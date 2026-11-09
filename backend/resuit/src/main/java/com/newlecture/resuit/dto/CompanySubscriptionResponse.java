package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

public record CompanySubscriptionResponse(
		String plan,
		boolean pro,
		boolean trial,
		LocalDateTime trialEndsAt,
		LocalDateTime expiresAt,
		boolean trialAvailable) {
}
