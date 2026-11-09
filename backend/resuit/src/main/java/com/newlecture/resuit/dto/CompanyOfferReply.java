package com.newlecture.resuit.dto;

// 받은 제안 응답을 서비스에 넘기는 값. 파라미터가 3개라 AGENTS.md 규칙대로 하나로 묶는다.
public record CompanyOfferReply(
		Long offerId,
		String username,
		String status) {
}
