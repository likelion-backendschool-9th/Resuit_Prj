package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

// 내 현직자 정보. 현직자 인증 화면에서 인증 상태와 함께 보여 주고 고친다.
public record MyMentorResponse(
		boolean verified,
		String email,
		String companyName,
		// 현직자 정보를 저장해 현직자 목록에 나오는지. 인증만 하고 정보를 저장하지 않았으면 false다.
		boolean registered,
		String jobTitle,
		Integer priceCredits,
		LocalDateTime verifiedAt) {
}
