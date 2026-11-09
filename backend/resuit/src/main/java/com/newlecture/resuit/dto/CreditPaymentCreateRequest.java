package com.newlecture.resuit.dto;

import jakarta.validation.constraints.NotNull;

public record CreditPaymentCreateRequest(
		@NotNull Integer credits) {
}
