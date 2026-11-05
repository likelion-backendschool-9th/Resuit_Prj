package com.newlecture.resuit.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.newlecture.resuit.entity.FeedbackComment;
import com.newlecture.resuit.entity.Mentor;

public record MentorDetailResponse(
		UUID id,
		String username,
		String nickname,
		String profileImageUrl,
		String selfIntroduction,
		String companyName,
		String jobTitle,
		Integer careerYears,
		int priceCredits,
		LocalDateTime verifiedAt,
		ReviewHistory reviewHistory) {

	public static MentorDetailResponse of(Mentor mentor, ReviewHistory reviewHistory) {
		return new MentorDetailResponse(
				mentor.getUserId(),
				mentor.getUser().getUsername(),
				mentor.getUser().getNickname(),
				mentor.getUser().getProfileImageUrl(),
				mentor.getUser().getSelfIntroduction(),
				mentor.getCompanyName(),
				mentor.getJobTitle(),
				mentor.getCareerYears(),
				mentor.getPriceCredits(),
				mentor.getVerifiedAt(),
				reviewHistory);
	}

	// 유료 피드백 내용은 요청자와 현직자만 볼 수 있어 공개 프로필에는 건수만 담는다.
	public record ReviewHistory(
			long completedPaidFeedbackCount,
			long publicReviewCount,
			long adoptedReviewCount,
			List<RecentReview> recentReviews) {
	}

	public record RecentReview(
			Long id,
			Long feedbackRequestId,
			String section,
			String content,
			boolean adopted,
			LocalDateTime createdAt) {

		public static RecentReview from(FeedbackComment comment) {
			return new RecentReview(
					comment.getCommentId(),
					comment.getFeedbackRequest().getId(),
					comment.getSection(),
					comment.getContent(),
					comment.isAdopted(),
					comment.getCreatedAt());
		}
	}
}
