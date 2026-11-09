package com.newlecture.resuit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// 받은 채용 제안에 응답하는 요청. status: ACCEPTED(승낙) 또는 DECLINED(거절)
public record CompanyOfferRespondRequest(
		@NotBlank @Pattern(regexp = "ACCEPTED|DECLINED") String status) {
}
