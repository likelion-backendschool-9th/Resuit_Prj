package com.newlecture.resuit.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record TalentSearchCondition(
		@Pattern(regexp = "FRONTEND|BACKEND") String field,
		@Min(0) Integer page,
		@Min(1) @Max(100) Integer size) {

	public Pageable toPageable() {
		return PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
	}
}
