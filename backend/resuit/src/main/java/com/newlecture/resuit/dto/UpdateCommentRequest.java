package com.newlecture.resuit.dto;

import java.util.UUID;

public record UpdateCommentRequest(
		UUID userId,
		String content) {
}
