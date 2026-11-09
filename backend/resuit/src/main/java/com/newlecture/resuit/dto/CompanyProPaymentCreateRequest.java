package com.newlecture.resuit.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyProPaymentCreateRequest(
		@NotBlank String product) {
}
