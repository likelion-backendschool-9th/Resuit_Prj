package com.newlecture.resuit.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CompanyOfferCreateRequest(
		@NotNull UUID userId,
		@Positive Long jobId,
		@NotBlank @Size(max = 2000) String message) {
}
