package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

import com.newlecture.resuit.entity.CompanyOffer;

// 채용 제안 응답이다. 보낸 결과(9.4)와 받은 제안 목록(9.5)이 같은 모양이라 함께 쓴다.
public record CompanyOfferResponse(
		Long id,
		// 보낸 기업 회원. 회사 이름은 기업 회원의 닉네임(nickname)으로 보여 준다.
		UserSummary company,
		// 보낸 기업의 이메일(기업 회원이 가입할 때 입력한 이메일)
		String companyEmail,
		UserSummary user,
		JobRef job,
		String message,
		String status,
		LocalDateTime createdAt) {

	public record JobRef(
			Long id,
			String title) {
	}

	// 채용 제안 엔티티를 응답 DTO로 바꾼다. 기업 이메일은 서비스에서 넘긴다.
	public static CompanyOfferResponse from(CompanyOffer offer, String companyEmail) {
		JobRef job = offer.getJob() == null ? null
				: new JobRef(offer.getJob().getId(), offer.getJob().getTitle());
		return new CompanyOfferResponse(
				offer.getId(),
				UserSummary.from(offer.getCompany()),
				companyEmail,
				UserSummary.from(offer.getUser()),
				job,
				offer.getMessage(),
				offer.getStatus(),
				offer.getCreatedAt());
	}
}
