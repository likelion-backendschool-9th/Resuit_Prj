package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

import com.newlecture.resuit.entity.CompanyOffer;

// 채용 제안 응답이다. 보낸 결과(9.4)와 받은 제안 목록(9.5)이 같은 모양이라 함께 쓴다.
public record CompanyOfferResponse(
		Long id,
		// 보낸 기업 회원. 회사 이름은 기업 회원의 닉네임(nickname)으로 보여 준다.
		UserSummary company,
		UserSummary user,
		JobRef job,
		String message,
		String status,
		LocalDateTime createdAt) {

	public record JobRef(
			Long id,
			String title) {
	}

	public static CompanyOfferResponse from(CompanyOffer offer) {
		JobRef job = offer.getJob() == null ? null
				: new JobRef(offer.getJob().getId(), offer.getJob().getTitle());
		return new CompanyOfferResponse(
				offer.getId(),
				UserSummary.from(offer.getCompany()),
				UserSummary.from(offer.getUser()),
				job,
				offer.getMessage(),
				offer.getStatus(),
				offer.getCreatedAt());
	}
}
