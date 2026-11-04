package com.newlecture.resuit.dto;

import java.util.UUID;

import com.newlecture.resuit.entity.Mentor;

public record MentorSummary(
		UUID id,
		String username,
		String nickname,
		String profileImageUrl,
		String companyName,
		String jobTitle,
		Integer careerYears,
		int priceCredits) {

	public static MentorSummary from(Mentor mentor) {
		return new MentorSummary(
				mentor.getUserId(),
				mentor.getUser().getUsername(),
				mentor.getUser().getNickname(),
				mentor.getUser().getProfileImageUrl(),
				mentor.getCompanyName(),
				mentor.getJobTitle(),
				mentor.getCareerYears(),
				mentor.getPriceCredits());
	}
}
