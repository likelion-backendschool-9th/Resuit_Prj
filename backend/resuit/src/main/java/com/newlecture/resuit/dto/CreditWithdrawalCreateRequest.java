package com.newlecture.resuit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreditWithdrawalCreateRequest(
		@NotNull Integer credits,
		@NotBlank @Size(max = 30) String bankName,
		@NotBlank @Size(max = 30) String accountNumber,
		@NotBlank @Size(max = 50) String accountHolder) {
}
