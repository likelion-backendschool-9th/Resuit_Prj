package com.newlecture.resuit.dto;

import jakarta.validation.constraints.NotNull;

public record GeneralJobApplicationRequest(
		@NotNull Long commonResumeId) {
}
