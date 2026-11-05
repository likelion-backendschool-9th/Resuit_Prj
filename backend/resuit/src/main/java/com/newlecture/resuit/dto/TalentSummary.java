package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

import com.newlecture.resuit.entity.CommonResume;

public record TalentSummary(
		UserSummary user,
		String field,
		Long resumeId,
		String resumeTitle,
		LocalDateTime updatedAt) {

	public static TalentSummary from(CommonResume resume) {
		return new TalentSummary(
				UserSummary.from(resume.getUser()),
				resume.getField(),
				resume.getId(),
				resume.getTitle(),
				resume.getUpdatedAt());
	}
}
