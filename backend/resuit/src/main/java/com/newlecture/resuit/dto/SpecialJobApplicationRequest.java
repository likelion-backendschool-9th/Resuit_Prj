package com.newlecture.resuit.dto;

import jakarta.validation.constraints.NotNull;

public record SpecialJobApplicationRequest(
		@NotNull Long targetResumeId) {
}
