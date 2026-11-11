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
		// 프로필 경력으로 계산한 경력 연수. 프로필 경력이 없으면 null이다.
		Integer careerYears,
		int priceCredits) {

	public static MentorSummary of(Mentor mentor, Integer careerYears) {
		return new MentorSummary(
				mentor.getUserId(),
				mentor.getUser().getUsername(),
				mentor.getUser().getNickname(),
				mentor.getUser().getProfileImageUrl(),
				mentor.getCompanyName(),
				mentor.getJobTitle(),
				careerYears,
				mentor.getPriceCredits());
	}
}
