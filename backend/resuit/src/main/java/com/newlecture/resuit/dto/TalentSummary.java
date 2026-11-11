package com.newlecture.resuit.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.newlecture.resuit.entity.CommonResume;
import com.newlecture.resuit.service.ReviewerBadgePolicy;

// 카드에 보여 줄 값(지역, 경력, 기여 수, 글 수, 배지)은 화면에 들어올 때 항상 필요해서 이 응답에 같이 담는다.
public record TalentSummary(
		UserSummary user,
		String field,
		Long resumeId,
		String resumeTitle,
		LocalDateTime updatedAt,
		String region,
		String career,
		int contributions,
		long posts,
		List<String> skills,
		// 가진 배지(ACTIVE, GRACE, EXPIRED). 프로필 응답과 같은 모양이라 재사용한다.
		List<ProfileResponse.BadgeSummary> badges,
		List<ProfileResponse.BadgeHistorySummary> badgeHistory,
		ReviewerBadgePolicy.Activity activity,
		// 조회한 기업이 이 회원에게 이미 채용 제안을 보냈는지. 제안은 한 번만 보낼 수 있다.
		boolean offered) {

	public static TalentSummary of(CommonResume resume, String career, int contributions, long posts,
			List<ProfileResponse.BadgeSummary> badges, List<ProfileResponse.BadgeHistorySummary> badgeHistory,
			ReviewerBadgePolicy.Activity activity, boolean offered) {
		return new TalentSummary(
				UserSummary.from(resume.getUser()),
				resume.getField(),
				resume.getId(),
				resume.getTitle(),
				resume.getUpdatedAt(),
				resume.getUser().getRegion(),
				career, contributions, posts,
				skills(resume),
				badges, badgeHistory, activity, offered);
	}

	private static List<String> skills(CommonResume resume) {
		if (resume.getContent() == null || !(resume.getContent().get("skills") instanceof List<?> list)) {
			return List.of();
		}
		return list.stream().map(String::valueOf).toList();
	}
}
