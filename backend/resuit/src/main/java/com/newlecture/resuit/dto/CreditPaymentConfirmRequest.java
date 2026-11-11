package com.newlecture.resuit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreditPaymentConfirmRequest(
		@NotBlank @Size(max = 200) String paymentKey,
		@NotBlank @Size(max = 64) String orderId,
		@NotNull @Positive Integer amount) {
}
