package com.newlecture.resuit.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CompanySubscriptionRequest(
		@NotNull @Pattern(regexp = "TRIAL") String plan) {
}
