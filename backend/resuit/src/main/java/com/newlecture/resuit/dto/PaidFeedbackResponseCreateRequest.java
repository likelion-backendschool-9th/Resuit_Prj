package com.newlecture.resuit.dto;

import jakarta.validation.constraints.NotBlank;

public record PaidFeedbackResponseCreateRequest(@NotBlank String content) {
}
