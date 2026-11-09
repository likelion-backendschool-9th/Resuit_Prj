package com.newlecture.resuit.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 현직자 정보 저장 요청. 회사명은 인증한 회사 이메일 도메인으로 정해지므로 받지 않는다.
public record MentorProfileRequest(
		@NotBlank @Size(max = 100) String jobTitle,
		@Min(0) @Max(60) Integer careerYears,
		@NotNull @Min(1) @Max(10000) Integer priceCredits) {
}
